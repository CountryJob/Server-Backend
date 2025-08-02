from fastapi import APIRouter, UploadFile, Form, HTTPException
from pydantic import BaseModel
from datetime import datetime, date
import os, json

from ai.voice.stt_module import transcribe_audio
from ai.voice.llm_processing import summarize_to_json
from ai.auto_filled.predictor import process_job_posting

router = APIRouter()

AUDIO_DIR = "ai/voice/audio"
OUTPUT_JSON = "ai/voice/job_posting_result.json"

def safe_parse_date(val):
    if isinstance(val, date):
        return val
    elif isinstance(val, datetime):
        return val.date()
    elif isinstance(val, str):
        # ISO 8601 처리: '2025-08-02T20:22:14.903Z' → '2025-08-02'
        cleaned = val.strip().replace("Z", "")
        return date.fromisoformat(cleaned[:10])
    else:
        raise ValueError(f"Invalid date format: {val} ({type(val)})")


class AutoFilledResponse(BaseModel):
    title: str
    startDate: str
    endDate: str
    startTime: str
    endTime: str
    areaSize: int
    meal: bool
    snack: bool
    transportAllowance: bool
    description: str
    salaryMale: int
    salaryFemale: int
    recruitCountMale: int
    recruitCountFemale: int
    experienceRequired: bool


@router.post("/predict", response_model=AutoFilledResponse)
async def auto_filled_predict(audio_file: UploadFile = Form(...)):
    try:
        # 1. 오디오 저장
        os.makedirs(AUDIO_DIR, exist_ok=True)
        file_path = os.path.join(AUDIO_DIR, "full_response.webm")
        with open(file_path, "wb") as f:
            f.write(await audio_file.read())

        # 2. STT 처리
        transcribed = transcribe_audio(file_path)

        # 3. LLM 정보 정제
        summarized_text = summarize_to_json(transcribed)
        with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
            f.write(summarized_text)
        info = json.loads(summarized_text)

        # 4. 작업 기간 계산
        print("Raw start_date:", info["start_date"], type(info["start_date"]))
        print("Raw end_date:", info["end_date"], type(info["end_date"]))

        start_date = safe_parse_date(info["start_date"])
        end_date = safe_parse_date(info["end_date"])

        print("Parsed start_date:", start_date, type(start_date))
        print("Parsed end_date:", end_date, type(end_date))
        print("🎙 STT transcribed text:", transcribed)

        duration_days = (end_date - start_date).days + 1
        if duration_days <= 0:
            raise ValueError("`end_date` must be after `start_date`.")

        # 5. 작업 시간 문자열 구성
        work_time = f"{info['start_time'][:5]} ~ {info['end_time'][:5]}"

        # 6. AI 예측 수행
        prediction = process_job_posting(
            title=info["title"],
            area_size=info["area_size"],
            start_date=start_date,
            end_date=end_date,
            work_time=work_time,
            duration_days=duration_days,
            verbose=False
        )

        # 7. 응답 구성 (CamelCase)
        response_data = {
            "title": info["title"],
            "startDate": info["start_date"],
            "endDate": info["end_date"],
            "startTime": info["start_time"],
            "endTime": info["end_time"],
            "areaSize": info["area_size"],
            "meal": info["meal"],
            "snack": info["snack"],
            "transportAllowance": info["transport_allowance"],
            "experienceRequired": info["experienceRequired"],
            "description": prediction["description"],
            "salaryMale": prediction["salary_male"],
            "salaryFemale": prediction["salary_female"],
            "recruitCountMale": prediction["recruit_count_male"],
            "recruitCountFemale": prediction["recruit_count_female"],
        }

        return response_data

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {e}")
