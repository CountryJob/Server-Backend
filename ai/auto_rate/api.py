from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from enum import Enum
from typing import List, Dict
from ai.auto_rate.trust_score import calculate_trust_score, ReviewRequest

router = APIRouter()


@router.post("/review")
def auto_rate_review(req: ReviewRequest)-> dict:
    try:
        score = calculate_trust_score(req)
        return {str(req.userId): score}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))