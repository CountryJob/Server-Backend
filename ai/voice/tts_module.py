import pyttsx3

def speak_text_korean(text):
    engine = pyttsx3.init()
    engine.setProperty('rate', 170)
    engine.setProperty('volume', 1.0)

    voices = engine.getProperty('voices')
    for voice in voices:
        if "Yuna" in voice.name:
            engine.setProperty('voice', voice.id)
            break

    engine.say(text)
    engine.runAndWait()
