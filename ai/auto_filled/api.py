from fastapi import APIRouter, UploadFile, Form, HTTPException
from pydantic import BaseModel
from fastapi.responses import JSONResponse
from datetime import date
import os, json

from ai.voice.stt_module import transcribe_audio
from ai.voice.llm_processing import summarize_to_json
from ai.auto_filled.predictor import process_job_posting

router = APIRouter()

AUDIO_DIR = "ai/voice/audio"
OUTPUT_JSON = "ai/voice/job_posting_result.json"

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

@router.post("/predict", response_model=AutoFilledResponse)
async def auto_filled_predict(audio_file: UploadFile = Form(...)):
    try:
        # 1. 오디오 저장
        file_path = os.path.join(AUDIO_DIR, "full_response.m4a")
        with open(file_path, "wb") as f:
            f.write(await audio_file.read())

        # 2. STT 처리
        transcribed = transcribe_audio(file_path)

        # 3. LLM으로 정보 정제
        summarized_text = summarize_to_json(transcribed)
        with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
            f.write(summarized_text)
        info = json.loads(summarized_text)

        # 4. 작업 기간 계산
        start_date = date.fromisoformat(info["start_date"])
        end_date = date.fromisoformat(info["end_date"])
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

        # 7. 카멜케이스 응답 구성
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
            "description": prediction["description"],
            "salaryMale": prediction["salary_male"],
            "salaryFemale": prediction["salary_female"],
            "recruitCountMale": prediction["recruit_count_male"],
            "recruitCountFemale": prediction["recruit_count_female"]
        }

        return response_data

    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Prediction error: {e}")
