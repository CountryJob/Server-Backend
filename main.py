from fastapi import FastAPI

# 각 기능별 라우터 임포트
from ai.auto_filled.api import router as autofill_router
from ai.recommend.api import router as recommend_router
from ai.auto_rate.api import router as trust_router


app = FastAPI(
    title="Farm4U AI API",
    description="음성 기반 공고 작성, 자동 채움, 추천 시스템, 신뢰 점수 평가 기능 제공",
    version="1.0.0"
)

# 기능별 라우터 등록
app.include_router(autofill_router, prefix="/auto-filled", tags=["Auto Description & Prediction"])
app.include_router(recommend_router, prefix="/recommend", tags=["Matching Recommendation"])
app.include_router(trust_router, prefix="/auto-rate", tags=["Trust Score Evaluation"])

# 실행 명령:
# uvicorn main:app --host 0.0.0.0 --port 8000 --reload
