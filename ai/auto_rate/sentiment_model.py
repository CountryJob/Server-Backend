from transformers import pipeline

# 최초 한 번만 로딩 (속도 향상)
sentiment_pipeline = pipeline("sentiment-analysis", model="beomi/KcELECTRA-base")

def analyze_sentiment_score(text: str) -> float:
    """
    HuggingFace 모델을 사용한 감정 분석 (0~1 실수 반환)
    """
    result = sentiment_pipeline(text[:512])[0]  # 너무 긴 문장은 자름
    label = result["label"]
    score = result["score"]

    # label: 'positive' or 'negative'
    if label == "positive":
        return min(score, 1.0)
    else:
        return 1.0 - score
