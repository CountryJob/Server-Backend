from ai.auto_rate.sentiment_model import analyze_sentiment_score
from typing import List, Optional, Literal
from enum import Enum
from pydantic import BaseModel

class TargetType(str, Enum):
    WORKER = "WORKER"
    FARMER = "FARMER"

class ReviewRequest(BaseModel):
    userId: int
    targetType: Literal["WORKER", "FARMER"]
    content: str

    # FARMER용
    communication_rating: Optional[float] = None
    environment_rating: Optional[float] = None
    clarity_rating: Optional[float] = None
    reward_rating: Optional[float] = None

    # WORKER용
    sincerity_rating: Optional[float] = None
    promise_rating: Optional[float] = None
    skill_rating: Optional[float] = None
    rehire_rating: Optional[float] = None

def calculate_trust_score(data: ReviewRequest) -> int:
    if data.targetType == "WORKER":
        weights = {
            "sincerity_rating": 0.3,
            "promise_rating": 0.2,
            "skill_rating": 0.2,
            "rehire_rating": 0.3
        }
    elif data.targetType == "FARMER":
        weights = {
            "communication_rating": 0.3,
            "environment_rating": 0.2,
            "clarity_rating": 0.3,
            "reward_rating": 0.2
        }
    else:
        raise ValueError("Invalid target type")

    # 평점 계산
    rating_score = 0
    total_weight = 0
    for key, weight in weights.items():
        value = getattr(data, key, None)
        if value is not None:
            rating_score += value * weight
            total_weight += weight
    avg_rating = rating_score / total_weight if total_weight > 0 else 3

    # 감정점수
    sentiment_score = analyze_sentiment_score(data.content)

    trust_score = avg_rating * 20 * 0.8 + sentiment_score * 100 * 0.2
    return int(round(trust_score))

