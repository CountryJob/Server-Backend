import json
from stt_module import transcribe_audio
from tts_module import speak_text_korean
from llm_processing import summarize_to_json
from utils import QUESTION_LIST

INTERIM_JSON = "ai/voice/interim_answers.json"
OUTPUT_JSON = "ai/voice/job_posting_result.json"

def run_voice_pipeline():
    answers = {}

    for key, question in QUESTION_LIST:
        # 음성 출력
        speak_text_korean(question)

        # 예시 오디오 파일 경로
        audio_path = f"ai/voice/audio/{key}.m4a"

        # Whisper STT
        print(f"[STT 중] {key}: {audio_path}")
        text = transcribe_audio(audio_path)
        print(f"[답변]: {text}")
        answers[key] = text

    # 중간 결과 저장
    with open(INTERIM_JSON, "w", encoding="utf-8") as f:
        json.dump(answers, f, ensure_ascii=False, indent=2)

    # 테스트용 farmer address (임시 고정)
    farmer_address = "전북특별자치도 진안군 부귀면 가치길 17-41"

    # Gemini LLM을 통한 정보 추출
    result_text = summarize_to_json(answers, farmer_address)

    # JSON 포맷 마크다운 제거
    cleaned_text = result_text.replace("```json", "").replace("```", "").strip()

    # 최종 결과 저장
    with open(OUTPUT_JSON, "w", encoding="utf-8") as f:
        f.write(cleaned_text)


    print("\n✅ 최종 결과:")
    print(cleaned_text)


if __name__ == "__main__":
    run_voice_pipeline()
