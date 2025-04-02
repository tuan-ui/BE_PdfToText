import numpy as np
import torch
import torchvision.transforms as T
from PIL import Image
from torchvision.transforms.functional import InterpolationMode
from transformers import AutoModel, AutoTokenizer
from vncorenlp import VnCoreNLP
from fastapi import FastAPI, File, UploadFile, HTTPException, Form
from fastapi.responses import JSONResponse
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import re
import json
import uvicorn
from io import BytesIO
import os
import shutil
from uuid import uuid4

annotator = VnCoreNLP("D:/VnCoreNLP/VnCoreNLP-1.1.1.jar", annotators="wseg,pos,ner,parse", max_heap_size='-Xmx2g')
# Hằng số cho ImageNet
IMAGENET_MEAN = (0.485, 0.456, 0.406)
IMAGENET_STD = (0.229, 0.224, 0.225)

# Hàm tạo transform cho ảnh
def build_transform(input_size):
    MEAN, STD = IMAGENET_MEAN, IMAGENET_STD
    transform = T.Compose([
        T.Lambda(lambda img: img.convert('RGB') if img.mode != 'RGB' else img),
        T.Resize((input_size, input_size), interpolation=InterpolationMode.BICUBIC),
        T.ToTensor(),
        T.Normalize(mean=MEAN, std=STD)
    ])
    return transform

# Hàm tìm tỷ lệ khung hình gần nhất
def find_closest_aspect_ratio(aspect_ratio, target_ratios, width, height, image_size):
    best_ratio_diff = float('inf')
    best_ratio = (1, 1)
    area = width * height
    for ratio in target_ratios:
        target_aspect_ratio = ratio[0] / ratio[1]
        ratio_diff = abs(aspect_ratio - target_aspect_ratio)
        if ratio_diff < best_ratio_diff:
            best_ratio_diff = ratio_diff
            best_ratio = ratio
        elif ratio_diff == best_ratio_diff:
            if area > 0.5 * image_size * image_size * ratio[0] * ratio[1]:
                best_ratio = ratio
    return best_ratio

# Hàm tiền xử lý ảnh động
def dynamic_preprocess(image, min_num=1, max_num=12, image_size=448, use_thumbnail=False):
    orig_width, orig_height = image.size
    aspect_ratio = orig_width / orig_height
    target_ratios = set(
        (i, j) for n in range(min_num, max_num + 1) for i in range(1, n + 1) for j in range(1, n + 1) if
        i * j <= max_num and i * j >= min_num)
    target_ratios = sorted(target_ratios, key=lambda x: x[0] * x[1])
    target_aspect_ratio = find_closest_aspect_ratio(
        aspect_ratio, target_ratios, orig_width, orig_height, image_size)
    target_width = image_size * target_aspect_ratio[0]
    target_height = image_size * target_aspect_ratio[1]
    blocks = target_aspect_ratio[0] * target_aspect_ratio[1]
    resized_img = image.resize((target_width, target_height))
    processed_images = []
    for i in range(blocks):
        box = (
            (i % (target_width // image_size)) * image_size,
            (i // (target_width // image_size)) * image_size,
            ((i % (target_width // image_size)) + 1) * image_size,
            ((i // (target_width // image_size)) + 1) * image_size
        )
        split_img = resized_img.crop(box)
        processed_images.append(split_img)
    assert len(processed_images) == blocks
    if use_thumbnail and len(processed_images) != 1:
        thumbnail_img = image.resize((image_size, image_size))
        processed_images.append(thumbnail_img)
    return processed_images

# Hàm load ảnh
def load_image(image_file, input_size=448, max_num=12):
    image = Image.open(image_file).convert('RGB')
    transform = build_transform(input_size=input_size)
    images = dynamic_preprocess(image, image_size=input_size, use_thumbnail=True, max_num=max_num)
    pixel_values = [transform(image) for image in images]
    pixel_values = torch.stack(pixel_values)
    return pixel_values

# Load model và tokenizer một lần duy nhất
model = AutoModel.from_pretrained(
    "5CD-AI/Vintern-1B-v3_5",
    torch_dtype=torch.bfloat16,
    low_cpu_mem_usage=False,
    trust_remote_code=True,
    use_flash_attn=False,
).eval().cuda()

tokenizer = AutoTokenizer.from_pretrained("5CD-AI/Vintern-1B-v3_5", trust_remote_code=True, use_fast=False)

# Hàm xử lý ảnh và câu hỏi
def process_image_and_question(image_path, question, history=None, max_num=12, input_size=448):
    pixel_values = load_image(image_path, max_num=max_num, input_size=input_size).to(torch.bfloat16).cuda()
    generation_config = dict(max_new_tokens=1024, do_sample=False, num_beams=3, repetition_penalty=2.5)
    response, history = model.chat(tokenizer, pixel_values, question, generation_config, history=history, return_history=True)
    return response, history

# Ví dụ sử dụng
cccd_patterns = {
    "Số thẻ": "cccd_number",
    "Họ và tên": "name",
    "Ngày sinh": "date_of_birth",
    "Giới tính": "gender",
    "Quốc tịch": "nationality",  
    "Quê quán": "place_of_origin",
    "Nơi thường trú": "place_of_residence",
    "Ngày hết hạn": "date_expired"
}
question = '<image>\nTrích xuất thông tin chính trong ảnh và trả về dạng markdown'

def process_change__key(response,cccd_patterns):
    if isinstance(response, tuple):
        response = response[0] 
    for key, value in cccd_patterns.items():
        response = response.replace(key, value)
    return response

app = FastAPI()
UPLOAD_DIR = "uploads"
os.makedirs(UPLOAD_DIR, exist_ok=True) 
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
) 
@app.post("/process")
async def process_text(file: UploadFile = File(...), request: str = Form(None)):
    filename = f"{uuid4().hex}.png"
    file_path = os.path.join(UPLOAD_DIR, filename)

    with open(file_path, "wb") as f:
        shutil.copyfileobj(file.file, f)

    try:
        response = process_image_and_question(file_path, question)
        responseEN = process_change__key(response, cccd_patterns)
        pattern = r"\* \*\*(.*?):\*\* (.*?)\n"
        matches = re.findall(pattern, responseEN)

        data = {key.strip(): value.strip() for key, value in matches}
        return data
    
    finally:
        if os.path.exists(file_path):
            os.remove(file_path)
if __name__ == "__main__":
    #  uvicorn.run("app:app", host="127.0.0.1", port=5001, reload=True)
     uvicorn.run(app, host="0.0.0.0", port=8000)
