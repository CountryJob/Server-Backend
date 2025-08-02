from datetime import date
import joblib
import numpy as np
import pandas as pd
import google.generativeai as genai
import re
import math
import sys
import os
import dotenv

# 환경 변수 로드
dotenv.load_dotenv()

# 모델 클래스를 위한 경로 추가
sys.path.append(os.path.dirname(os.path.abspath(__file__)))

# 환경 설정
genai.configure(api_key=os.getenv("GOOGLE_API_KEY"))

work_type_info = {
    "available_types": [
        "복분자수확(복분자)",
        "배 봉지 싸기 등(배, 참깨)",
        "복분자따기(복분자)",
        "잔디 심기(잔디)",
        "블루베리 수확 및 포장(블루베리)",
        "상추 수확 및 상차(상추)",
        "기타작업"
    ],
    "work_characteristics": {
        "여성중심": {
            "keywords": ["포장", "봉지", "싸기", "선별", "분류", "정리", "박스"],
            "female_ratio": 0.8,
            "description": "정교하고 세밀한 작업"
        },
        "남성중심": {
            "keywords": ["상차", "운반", "나무", "무거운", "힘", "들어올리기", "옮기기"],
            "female_ratio": 0.2,
            "description": "체력이 필요한 작업"
        },
        "혼합작업": {
            "keywords": ["수확", "따기", "심기", "관리", "돌보기"],
            "female_ratio": 0.5,
            "description": "남녀 구분 없는 작업"
        }
    },
    "crop_preferences": {
        "세밀한작물": {
            "crops": ["블루베리", "복분자", "딸기", "방울토마토"],
            "female_ratio": 0.7,
            "reason": "손상되기 쉬운 작물로 세심함 필요"
        },
        "일반작물": {
            "crops": ["상추", "배추", "양파", "고추"],
            "female_ratio": 0.5,
            "reason": "일반적인 농작업"
        }
    },
    "keyword_groups": {
        "수확": [
            "복분자수확(복분자)", "복분자따기(복분자)", "블루베리 수확 및 포장(블루베리)", "상추 수확 및 상차(상추)"
        ],
        "따기": ["복분자따기(복분자)", "복분자수확(복분자)"],
        "포장": ["블루베리 수확 및 포장(블루베리)", "기타작업"],
        "봉지": ["배 봉지 싸기 등(배, 참깨)", "기타작업"],
        "싸기": ["배 봉지 싸기 등(배, 참깨)", "기타작업"],
        "상추": ["상추 수확 및 상차(상추)"],
        "블루베리": ["블루베리 수확 및 포장(블루베리)"],
        "복분자": ["복분자수확(복분자)", "복분자따기(복분자)"],
        "심기": ["잔디 심기(잔디)"],
        "잔디": ["잔디 심기(잔디)"],
        "상차": ["상추 수확 및 상차(상추)"],
        "선별": ["기타작업"],
        "정리": ["기타작업"],
        "정돈": ["기타작업"],
        "관리": ["기타작업"],
        "작업": ["기타작업"],
        "풀매기": ["기타작업"],
        "묘목": ["기타작업"],
        "채우기": ["기타작업"],
        "옮기기": ["기타작업"],
        "운반": ["기타작업"],
        "상하차": ["기타작업"],
        "포장작업": ["기타작업"]
    },
    "default_type": "기타작업"
}

# train_multi_model.py와 동일한 모델 클래스 정의
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.compose import ColumnTransformer
from sklearn.linear_model import LogisticRegression

class WorkforceWageModel:
    def __init__(self):
        self.count_models = {}
        self.wage_classifiers = {}
        self.wage_regressors = {}
        self.preprocessor = None
        self.wage_medians = {}
        
    def fit(self, X, y):
        # 전처리 파이프라인 정의
        numerical_features = [
            'area_num', 'duration_num', 'log_area', 'log_duration',
            'area_per_day', 'has_male', 'has_female', 'both_gender',
            'has_male_wage', 'has_female_wage', 'wage_ratio'
        ]
        categorical_features = ['region_category', 'season', 'work_scale', 'duration_category']
        
        # work_type에서 추출된 특성들의 컬럼명들
        work_feature_cols = [col for col in X.columns if col not in numerical_features + categorical_features]
        
        self.preprocessor = ColumnTransformer([
            ('num', StandardScaler(), numerical_features),
            ('cat', OneHotEncoder(sparse_output=False, handle_unknown='ignore', drop='first'), categorical_features),
            ('work', 'passthrough', work_feature_cols)
        ])
        
        X_processed = self.preprocessor.fit_transform(X)
        
        # 1. 인원수 예측 모델
        for target in ['male_count', 'female_count']:
            model = RandomForestRegressor(
                n_estimators=150,
                max_depth=12,
                min_samples_split=3,
                min_samples_leaf=1,
                random_state=42
            )
            
            target_idx = ['male_count', 'female_count', 'wage_male', 'wage_female'].index(target)
            model.fit(X_processed, y.iloc[:, target_idx])
            self.count_models[target] = model
        
        # 2. 임금 예측 모델
        for target in ['wage_male', 'wage_female']:
            target_idx = ['male_count', 'female_count', 'wage_male', 'wage_female'].index(target)
            wage_values = y.iloc[:, target_idx]
            count_target = 'male_count' if target == 'wage_male' else 'female_count'  
            count_idx = ['male_count', 'female_count', 'wage_male', 'wage_female'].index(count_target)
            count_values = y.iloc[:, count_idx]
            
            # 임금이 있는 경우의 중간값 저장
            valid_wage_mask = (wage_values > 0) & (count_values > 0)
            if valid_wage_mask.sum() > 0:
                self.wage_medians[target] = wage_values[valid_wage_mask].median()
            else:
                self.wage_medians[target] = 100000
            
            # 임금 분류기
            wage_binary = valid_wage_mask.astype(int)
            
            if len(np.unique(wage_binary)) > 1:
                classifier = LogisticRegression(random_state=42, max_iter=1000, class_weight='balanced')
                classifier.fit(X_processed, wage_binary)
                self.wage_classifiers[target] = classifier
            else:
                self.wage_classifiers[target] = None
            
            # 임금 회귀기
            if valid_wage_mask.sum() > 5:
                X_valid = X_processed[valid_wage_mask]
                y_valid = wage_values[valid_wage_mask]
                
                regressor = GradientBoostingRegressor(
                    n_estimators=120,
                    max_depth=8,
                    learning_rate=0.12,
                    random_state=42,
                    subsample=0.8
                )
                regressor.fit(X_valid, y_valid)
                self.wage_regressors[target] = regressor
            else:
                self.wage_regressors[target] = None
                
        return self
    
    def predict(self, X):
        X_processed = self.preprocessor.transform(X)
        predictions = []
        
        # 1. 인원수 예측
        male_count_pred = np.maximum(0, self.count_models['male_count'].predict(X_processed))
        female_count_pred = np.maximum(0, self.count_models['female_count'].predict(X_processed))
        
        predictions.append(male_count_pred)
        predictions.append(female_count_pred)
        
        # 2. 임금 예측
        for target in ['wage_male', 'wage_female']:
            count_pred = male_count_pred if target == 'wage_male' else female_count_pred
            
            wage_pred = np.zeros(len(X_processed))
            
            # 인원이 있는 경우만 임금 예측
            has_count_indices = np.where(count_pred > 0.3)[0]
            
            if len(has_count_indices) > 0:
                X_has_count = X_processed[has_count_indices]
                
                # 임금 여부 분류
                if self.wage_classifiers[target] is not None:
                    wage_prob = self.wage_classifiers[target].predict_proba(X_has_count)
                    has_wage_prob = wage_prob[:, 1] if wage_prob.shape[1] > 1 else np.ones(len(X_has_count))
                    has_wage_mask = has_wage_prob > 0.3
                else:
                    has_wage_mask = np.ones(len(X_has_count), dtype=bool)
                
                # 임금 예측
                wage_indices = has_count_indices[has_wage_mask]
                if len(wage_indices) > 0 and self.wage_regressors[target] is not None:
                    X_wage = X_processed[wage_indices]
                    wage_values = self.wage_regressors[target].predict(X_wage)
                    
                    # 임금 범위 조정
                    wage_values = np.clip(wage_values, 50000, 300000)
                    wage_pred[wage_indices] = wage_values
                elif len(wage_indices) > 0:
                    # 회귀 모델이 없으면 중간값 사용
                    wage_pred[wage_indices] = self.wage_medians[target]
            
            predictions.append(wage_pred)
        
        return np.column_stack(predictions)

# 모델 로드 (재시도 로직 포함)
model_pipeline = None
model_files = [
    "ai/auto_filled/models/work_type_aware_model.pkl"
]

for model_file in model_files:
    try:
        if os.path.exists(model_file):
            model_pipeline = joblib.load(model_file)
            print(f"✓ 모델 로드 성공: {model_file}")
            break
    except Exception as e:
        print(f"✗ 모델 로드 실패 ({model_file}): {e}")
        continue

if model_pipeline is None:
    print("⚠️  모델을 찾을 수 없습니다. 기본 예측 모드로 실행합니다.")

def analyze_work_characteristics(title):
    """작업 특성을 더 정교하게 분석"""
    title_lower = title.lower()
    characteristics = []
    female_ratio = 0.5  # 기본값
    
    # 작업 특성 분석
    for char_type, info in work_type_info["work_characteristics"].items():
        matched_keywords = [kw for kw in info["keywords"] if kw in title_lower]
        if matched_keywords:
            characteristics.append({
                "type": char_type,
                "keywords": matched_keywords,
                "female_ratio": info["female_ratio"],
                "description": info["description"]
            })
    
    # 작물 특성 분석
    for crop_type, info in work_type_info["crop_preferences"].items():
        matched_crops = [crop for crop in info["crops"] if crop in title_lower]
        if matched_crops:
            characteristics.append({
                "type": f"작물_{crop_type}",
                "keywords": matched_crops,
                "female_ratio": info["female_ratio"],
                "description": info["reason"]
            })
    
    # 우선순위에 따른 최종 비율 결정
    if characteristics:
        # 포장 작업이 있으면 최우선
        for char in characteristics:
            if char["type"] == "여성중심":
                female_ratio = char["female_ratio"]
                break
        # 체력 작업이 있으면 두 번째 우선
        for char in characteristics:
            if char["type"] == "남성중심" and female_ratio == 0.5:
                female_ratio = char["female_ratio"]
                break
    
    return characteristics, female_ratio

def find_best_work_type(input_work_type, work_type_info):
    """입력된 작업 유형을 기존 학습 데이터의 작업 유형과 매칭"""
    available_types = work_type_info['available_types']
    keyword_groups = work_type_info['keyword_groups']
    default_type = work_type_info['default_type']
    
    input_lower = input_work_type.lower().strip()
    
    # 1) 정확히 일치하는 경우
    for available in available_types:
        if input_work_type == available:
            return available, "정확일치"
    
    # 2) 부분 문자열 포함 체크
    for available in available_types:
        available_lower = available.lower()
        if input_lower in available_lower or available_lower in input_lower:
            return available, "부분일치"
    
    # 3) 작물명 기반 매칭
    crop_patterns = [
        r'(상추|블루베리|복숭아|복분자|들깨|참깨|콩|고추|옥수수|사과|포도|가지|부추|배추|무|당근|배)',
        r'(\w+)(?:수확|심기|재배)'
    ]
    
    for pattern in crop_patterns:
        match = re.search(pattern, input_work_type)
        if match:
            crop = match.group(1)
            for available in available_types:
                if crop in available:
                    return available, f"작물매칭({crop})"
    
    # 4) 키워드 매칭
    matched_keywords = []
    for keyword, keyword_types in keyword_groups.items():
        if keyword_types and keyword in input_lower:
            matched_keywords.append((keyword, keyword_types))
    
    if matched_keywords:
        best_keyword, best_types = min(matched_keywords, key=lambda x: len(x[1]))
        return best_types[0], f"키워드매칭({best_keyword})"
    
    return default_type, "기본값"

def smart_round(value):
    """스마트 반올림 함수"""
    if value <= 0.2:
        return 0
    elif value <= 0.7:
        return 1 if np.random.random() < (value - 0.2) / 0.5 else 0
    else:
        base = int(value)
        remainder = value - base
        if remainder >= 0.6:
            return base + 1
        elif remainder >= 0.3:
            return base + (1 if np.random.random() < remainder else 0)
        else:
            return base

def create_basic_prediction(title, area, duration_days):
    """모델이 없을 때 기본 예측"""
    characteristics, female_ratio = analyze_work_characteristics(title)
    
    # 면적과 기간에 따른 기본 인력 계산
    base_workforce = max(1, min(10, int(area / 50) + int(duration_days / 2)))
    
    female_count = int(base_workforce * female_ratio)
    male_count = base_workforce - female_count
    
    # 최소 1명 보장
    if male_count == 0 and female_count == 0:
        if female_ratio >= 0.5:
            female_count = 1
        else:
            male_count = 1
    
    # 기본 임금 (작업 특성에 따라)
    base_wage = 120000
    if any(char["type"] == "여성중심" for char in characteristics):
        base_wage = 100000
    elif any(char["type"] == "남성중심" for char in characteristics):
        base_wage = 140000
    
    return {
        'male_count': male_count,
        'female_count': female_count,
        'wage_male': base_wage if male_count > 0 else 0,
        'wage_female': base_wage if female_count > 0 else 0,
        'characteristics': characteristics,
        'female_ratio': female_ratio
    }

def predict_workforce_and_wage(title, area, duration_days, verbose=False):
    """향상된 인력 및 임금 예측 함수"""
    try:
        # 1. 작업 유형 매칭
        matched_work_type, match_reason = find_best_work_type(title, work_type_info)
        
        # 2. 작업 특성 분석
        characteristics, target_female_ratio = analyze_work_characteristics(title)
        
        if verbose:
            print(f"작업 유형 매칭: '{title}' → '{matched_work_type}' ({match_reason})")
            print(f"작업 특성 분석: {[c['type'] for c in characteristics]}")
            print(f"목표 여성 비율: {target_female_ratio:.1%}")
        
        # 3. 모델 예측 또는 기본 예측
        if model_pipeline is not None:
            # 모델 입력 데이터 생성 (간소화)
            input_data = pd.DataFrame({
                'work_type': [matched_work_type],
                'area_num': [area],
                'duration_num': [duration_days]
            })
            
            try:
                predictions = model_pipeline.predict(input_data)[0]
                base_male, base_female = predictions[0], predictions[1]
                base_wage_male = predictions[2] if len(predictions) > 2 else 100000
                base_wage_female = predictions[3] if len(predictions) > 3 else 100000
            except:
                # 모델 예측 실패 시 기본 예측 사용
                basic_pred = create_basic_prediction(title, area, duration_days)
                base_male = basic_pred['male_count']
                base_female = basic_pred['female_count']
                base_wage_male = basic_pred['wage_male']
                base_wage_female = basic_pred['wage_female']
        else:
            # 모델이 없을 때 기본 예측
            basic_pred = create_basic_prediction(title, area, duration_days)
            base_male = basic_pred['male_count']
            base_female = basic_pred['female_count']
            base_wage_male = basic_pred['wage_male']
            base_wage_female = basic_pred['wage_female']
        
        if verbose:
            print(f"기본 예측: 남성 {base_male:.2f}명, 여성 {base_female:.2f}명")
            print(f"기본 임금: 남성 {base_wage_male:.0f}원, 여성 {base_wage_female:.0f}원")
        
        # 4. 작업 특성에 따른 조정
        total_workforce = base_male + base_female
        if total_workforce > 0:
            adjusted_female = total_workforce * target_female_ratio
            adjusted_male = total_workforce * (1 - target_female_ratio)
        else:
            adjusted_male, adjusted_female = base_male, base_female
        
        # 5. 최종 인원 결정
        male_count = smart_round(adjusted_male)
        female_count = smart_round(adjusted_female)
        
        # 6. 최소 인원 보장
        if male_count == 0 and female_count == 0:
            if target_female_ratio >= 0.5:
                female_count = 1
            else:
                male_count = 1
        
        # 7. 임금 조정
        final_wage_male = base_wage_male if male_count > 0 else 0
        final_wage_female = base_wage_female if female_count > 0 else 0
        
        # 임금 범위 조정
        if final_wage_male > 0:
            final_wage_male = max(80000, min(200000, final_wage_male))
        if final_wage_female > 0:
            final_wage_female = max(80000, min(200000, final_wage_female))
        
        if verbose:
            print(f"조정 후: 남성 {adjusted_male:.2f}명, 여성 {adjusted_female:.2f}명")
            print(f"최종 결과: 남성 {male_count}명, 여성 {female_count}명")
            print(f"최종 임금: 남성 {final_wage_male:.0f}원, 여성 {final_wage_female:.0f}원")
        
        return {
            'male_count': male_count,
            'female_count': female_count,
            'wage_male': int(final_wage_male),
            'wage_female': int(final_wage_female),
            'characteristics': characteristics,
            'female_ratio': target_female_ratio,
            'matched_work_type': matched_work_type,
            'match_reason': match_reason
        }
        
    except Exception as e:
        print(f"예측 오류: {e}")
        return {
            'male_count': 1,
            'female_count': 1,
            'wage_male': 100000,
            'wage_female': 100000,
            'characteristics': [],
            'female_ratio': 0.5,
            'matched_work_type': "기타작업",
            'match_reason': "오류"
        }

def generate_description_llm(title, area, start_date, end_date, work_time):
    """LLM으로 작업 설명 생성"""
    prompt = f"""
    농촌 일자리 작업 내용을 4개의 간단한 bullet point로 작성해주세요.
    각 항목은 '-'로 시작하고 한 줄로 간결하게 작성해주세요.
    - 각 줄은 **한 줄 내 요약된 작업 설명**이어야 합니다.
    - 마침표 없이 작성해주세요.
    - 너무 긴 문장은 피하고, **짧고 실용적인 구 형태로** 작성해주세요.

    작업: {title}
    면적: {area}평
    기간: {start_date} ~ {end_date}
    시간: {work_time}
    예시:
    - 상추 수확 및 선별  
    - 작업 도구 정리  
    - 박스 포장 및 이동 보조  
    - 농장 주변 정돈    

    """
    
    try:
        model_ai = genai.GenerativeModel("gemini-2.5-flash")
        response = model_ai.generate_content(prompt)
        content = response.text.strip()
        
        lines = []
        for line in content.splitlines():
            line = line.strip()
            if line.startswith("-") or line.startswith("•") or line.startswith("*"):
                clean_line = line if line.startswith("-") else "- " + line[1:].strip()
                lines.append(clean_line)
                if len(lines) >= 4:
                    break
        
        if len(lines) < 4:
            defaults = [
                f"- {title} 작업",
                "- 수확물 정리 및 선별", 
                "- 작업 도구 관리",
                "- 농장 정리 정돈"
            ]
            lines = (lines + defaults)[:4]
        
        return "\n".join(lines)
        
    except Exception as e:
        print(f"LLM 생성 오류: {e}")
        return f"""- {title} 작업
        - 수확물 정리 및 선별
        - 작업 도구 관리  
        - 농장 정리 정돈"""

def analyze_work_type(workforce_result):
    """작업 유형 분석 결과 텍스트 생성"""
    male_count = workforce_result['male_count']
    female_count = workforce_result['female_count']
    
    if female_count == 0:
        return "남성 인력 전용 (체력 집약적 작업)"
    elif male_count == 0:
        return "여성 인력 전용 (정교함이 요구되는 작업)"
    elif female_count > male_count:
        return f"여성 중심 작업 ({female_count}:{male_count})"
    elif male_count > female_count:
        return f"남성 중심 작업 ({male_count}:{female_count})"
    else:
        return f"남녀 균형 작업 ({male_count}:{female_count})"

def process_job_posting(title, area_size, start_date, end_date, work_time, duration_days, verbose=True):
    """개선된 일자리 정보 처리"""
    if verbose:
        print(f"\n=== 농촌 일자리 정보 처리 ===")
        print(f"작업명: {title}")
        print(f"면적: {area_size:,}평")
        print(f"기간: {start_date} ~ {end_date} ({duration_days}일)")
        print(f"시간: {work_time}")
    
    # AI 작업 내용 생성
    if verbose:
        print("\n🤖 AI가 작업 내용을 생성하는 중...")
    description = generate_description_llm(title, area_size, start_date, end_date, work_time)
    
    # 인력 수요 및 임금 예측
    if verbose:
        print("📊 인력 수요 및 임금을 예측하는 중...")
    workforce_result = predict_workforce_and_wage(title, area_size, duration_days, verbose=verbose)
    
    # 결과 정리
    result = {
        'description': description,
        'salary_male': workforce_result['wage_male'],
        'salary_female': workforce_result['wage_female'],
        'recruit_count_male': workforce_result['male_count'],
        'recruit_count_female': workforce_result['female_count'],
    }
    
    return result