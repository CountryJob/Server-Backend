# ai/recommend/api.py
from fastapi import APIRouter
from fastapi.responses import JSONResponse
from datetime import datetime, timedelta
from pydantic import BaseModel
from typing import List

router = APIRouter()

class JobInput(BaseModel):
    start_date: str
    end_date: str
    experience_required: bool
    recruit_count_male: int
    recruit_count_female: int

class WorkerInput(BaseModel):
    user_id: int
    gender: str
    has_farm_exp: bool
    trust_score: int
    work_days: List[str]

class WorkerRecommendationRequest(BaseModel):
    job: JobInput
    workers: List[WorkerInput]

class JobInputWithId(JobInput):
    id: int

class JobRecommendationRequest(BaseModel):
    worker: WorkerInput
    jobs: List[JobInputWithId]


class WorkerScore(BaseModel):
    workerId: int
    score: float

class JobScore(BaseModel):
    jobId: int
    score: float

class WorkerRecommendationResponse(BaseModel):
    results: List[WorkerScore]

class JobRecommendationResponse(BaseModel):
    results: List[JobScore]


# 날짜 범위 내 요일 리스트 추출 함수
def get_weekdays_between(start_date_str, end_date_str):
    start_date = datetime.strptime(start_date_str, "%Y-%m-%d")
    end_date = datetime.strptime(end_date_str, "%Y-%m-%d")
    weekdays = []
    for i in range((end_date - start_date).days + 1):
        day = start_date + timedelta(days=i)
        weekdays.append(day.strftime("%a").upper()[:3])  # 'MON', 'TUE', ...
    return weekdays

# 구직자 추천
@router.post("/workers-to-job", response_model=WorkerRecommendationResponse)
def recommend_workers_to_job(body: WorkerRecommendationRequest):
    job = body.job.dict()
    workers = [w.dict() for w in body.workers]

    job_weekdays = get_weekdays_between(job["start_date"], job["end_date"])

    results = []
    for w in workers:
        score = 0.5

        # 1. 경험자 우대
        if job.get("experience_required"):
            score += 0.3 if w.get("has_farm_exp") else -0.1

        # 2. 성별 선호 반영
        preferred_gender = "mixed"
        if job.get("recruit_count_male", 0) > job.get("recruit_count_female", 0):
            preferred_gender = "MALE"
        elif job.get("recruit_count_female", 0) > job.get("recruit_count_male", 0):
            preferred_gender = "FEMALE"

        if preferred_gender == w.get("gender"):
            score += 0.1

        # 3. 신뢰 점수 (0~100 → 0~0.2 가산)
        if w.get("trust_score") is not None:
            score += min(w["trust_score"] / 100 * 0.2, 0.2)

        # 4. work_days 매칭
        worker_days = w.get("work_days", [])
        matched_days = set(job_weekdays) & set(worker_days)
        match_ratio = len(matched_days) / len(job_weekdays) if job_weekdays else 0
        score += match_ratio * 0.2  # 최대 +0.2

        score = round(min(max(score, 0), 1), 2)
        results.append({"workerId": w["user_id"], "score": score})

    return {"results": results} 

# 공고 추천
@router.post("/jobs-to-worker", response_model=JobRecommendationResponse)
def recommend_jobs_to_worker(body: JobRecommendationRequest):
    worker = body.worker.dict()
    jobs = [j.dict() for j in body.jobs]

    results = []
    for j in jobs:
        score = 0.5

        # 1. 경험자 우대
        if j.get("experience_required"):
            score += 0.3 if worker.get("has_farm_exp") else -0.1

        # 2. 성별 선호 반영
        preferred_gender = "mixed"
        if j.get("recruit_count_male", 0) > j.get("recruit_count_female", 0):
            preferred_gender = "MALE"
        elif j.get("recruit_count_female", 0) > j.get("recruit_count_male", 0):
            preferred_gender = "FEMALE"

        if preferred_gender == worker.get("gender"):
            score += 0.1

        # 3. 신뢰 점수 (0~100 → 0~0.2 가산)
        if worker.get("trust_score") is not None:
            score += min(worker["trust_score"] / 100 * 0.2, 0.2)

        # 4. work_days 매칭
        worker_days = set(worker.get("work_days", []))
        job_weekdays = get_weekdays_between(j["start_date"], j["end_date"])
        matched_days = set(job_weekdays) & worker_days
        match_ratio = len(matched_days) / len(job_weekdays) if job_weekdays else 0
        score += match_ratio * 0.2

        score = round(min(max(score, 0), 1), 2)
        results.append({"jobId": j["id"], "score": score})

    return {"results": results} 
