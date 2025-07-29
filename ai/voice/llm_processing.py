# gemini로 정보 정제
import os
import google.generativeai as genai
import json
from dotenv import load_dotenv

load_dotenv()
genai.configure(api_key=os.getenv("GOOGLE_API_KEY"))

def summarize_to_json(answer_dict, farmer_address):
    prompt = """
다음은 농장 구인공고 작성을 위한 음성 응답입니다. 이를 기반으로 아래 필드의 값을 추론하여 JSON으로 만들어주세요:

- work_type
- date_range
- time_range
- area_size
- provide_meal (True/False)
- provide_snack (True/False)
- provide_transport (True/False)
- address_match (True/False)

농장 주소는 다음과 같습니다:
""" + farmer_address + """

답변 예시:
{ "work_type": "사과 수확", "date_range": "2025-08-01 ~ 2025-08-03", "time_range": "08:00 ~ 17:00" ... }

사용자 응답:
"""
    for key, val in answer_dict.items():
        prompt += f"\n[{key}] {val}"

    response = genai.GenerativeModel("gemini-2.5-flash").generate_content(prompt)
    return response.text