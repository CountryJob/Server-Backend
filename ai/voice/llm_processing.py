# gemini로 정보 정제
import os
import re
import google.generativeai as genai
import json
from dotenv import load_dotenv

load_dotenv()
genai.configure(api_key=os.getenv("GOOGLE_API_KEY"))

def summarize_to_json(transcribed: str):
    prompt = f"""
    다음은 농장 구인 공고 작성을 위한 **음성 응답 텍스트**입니다. 이 응답을 바탕으로 아래의 항목들을 추론하여 JSON 형태로 반환해주세요.

    추론해야 할 항목:
    - title: 어떤 종류의 농작업인지 (예: "상추 수확")
    - start_date: 작업 시작일 (형식: YYYY-MM-DD)
    - end_date: 작업 종료일 (형식: YYYY-MM-DD)
    - start_time: 작업 시작 시각 (형식: HH:MM:SS)
    - end_time: 작업 종료 시각 (형식: HH:MM:SS)
    - area_size: 면적 (숫자, 단위 제외)
    - meal: 점심 식사 제공 여부 (true/false)
    - snack: 간식 제공 여부 (true/false)
    - transport_allowance: 교통비 제공 여부 (true/false)
    - experienceRequired: 경험 필요 여부 (true/false) # 얘는 농작업 종류에 따라 추론

    **주의사항**
    - 날짜와 시간은 명확하게 포맷에 맞춰주세요.
    - 연도가 언급되지 않은 경우, 기본적으로 **2025년**으로 간주하여 날짜를 작성해주세요.
    - JSON 이외의 설명 없이 순수 JSON만 반환해주세요.
    - 만약 응답에서 필요한 정보가 누락되었다면 임의의 값으로 채워줘

    사용자 응답:
    {transcribed}
    """
    model = genai.GenerativeModel("gemini-2.5-flash")
    response = model.generate_content(prompt)
    # JSON 부분만 추출
    json_match = re.search(r'{[\s\S]+}', response.text)
    if not json_match:
        raise ValueError("LLM 응답에서 JSON을 추출할 수 없습니다.")
    return json_match.group(0)