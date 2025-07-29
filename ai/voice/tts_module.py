# 질문 음성 출력
import pyttsx3

def speak_text_korean(text):
    engine = pyttsx3.init()
    engine.setProperty('rate', 180)  # 말 속도
    engine.setProperty('volume', 1.0)

    voices = engine.getProperty('voices')
    for voice in voices:
        if "ko" in voice.id or "Korean" in voice.name:
            engine.setProperty('voice', voice.id)
            break

    engine.say(text)
    engine.runAndWait()