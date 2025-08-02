# 음성 인식
import os
import whisper

def transcribe_audio(audio_path):
    model = whisper.load_model("base")
    result = model.transcribe(audio_path, language='ko')
    return result['text'].strip()