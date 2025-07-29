import pandas as pd
import numpy as np
from sklearn.preprocessing import OneHotEncoder, StandardScaler
from sklearn.multioutput import MultiOutputRegressor
from sklearn.ensemble import RandomForestRegressor, GradientBoostingRegressor
from sklearn.model_selection import train_test_split
from sklearn.compose import ColumnTransformer
from sklearn.pipeline import Pipeline
from sklearn.metrics import mean_squared_error, r2_score, mean_absolute_error
from sklearn.linear_model import LogisticRegression
from sklearn.feature_extraction.text import TfidfVectorizer
import joblib
import os
import warnings
import re
warnings.filterwarnings('ignore')

# 1. 디렉토리 생성
os.makedirs("ai/models", exist_ok=True)

# 2. 데이터 로드
df = pd.read_csv("ai/data/ilmoa_jobs2.csv")
print(f"원본 데이터 크기: {len(df)}")

# 3. 모집 인원 추출
pattern = r'남\s*:\s*(\d+).*여\s*:\s*(\d+)'
extracted = df['recruitment_count'].str.extract(pattern, expand=False)
df = df[~extracted.isna().any(axis=1)].copy()
df[['male_count','female_count']] = extracted.astype(int)

# 4. 면적, 기간 처리
df['area_num'] = pd.to_numeric(df['area'].astype(str).str.replace(r'[^\d.]', '', regex=True), errors='coerce')
df['duration_num'] = pd.to_numeric(df['duration_days'], errors='coerce')
df['area_num'].fillna(df['area_num'].median(), inplace=True)
df['duration_num'].fillna(df['duration_num'].median(), inplace=True)

# 5. 임금 데이터 처리
df['wage_male'] = pd.to_numeric(df['wage_male'].str.replace(r'[^\d.]', '', regex=True), errors='coerce')
df['wage_female'] = pd.to_numeric(df['wage_female'].str.replace(r'[^\d.]', '', regex=True), errors='coerce')
df['wage_male'] = df['wage_male'].fillna(0)
df['wage_female'] = df['wage_female'].fillna(0)

# 6. work_type 텍스트 분석 및 키워드 추출
def extract_work_features(work_type_text):
    """작업 유형 텍스트에서 의미있는 특성들을 추출"""
    if pd.isna(work_type_text):
        return {}
    
    text = str(work_type_text).lower()
    
    # 주요 작물/작업 키워드
    crop_keywords = {
        '수확': ['수확', '따기', '뽑기', '캐기'],
        '파종': ['심기', '파종', '모종', '정식'],
        '관리': ['관리', '정리', '정돈', '손질', '제거'],
        '포장': ['포장', '상차', '선별', '분류', '봉지', '싸기'],
        '재배': ['재배', '키우기', '기르기'],
        '농약': ['농약', '방제', '살포'],
        '수확후': ['건조', '세척', '청소']
    }
    
    crop_types = {
        '과일': ['블루베리', '복숭아', '복분자', '사과', '배', '포도', '딸기', '참외', '수박'],
        '채소': ['상추', '고추', '호박', '양파', '콩', '토마토', '오이', '배추', '무'],
        '곡물': ['벼', '쌀', '보리', '밀', '옥수수'],
        '특용작물': ['인삼', '새싹삼', '버섯', '화훼'],
        '기타': ['조경수', '잔디', '묘목', '축사', '소', '닭']
    }
    
    features = {}
    
    # 작업 유형 특성
    for work_type, keywords in crop_keywords.items():
        features[f'work_{work_type}'] = int(any(keyword in text for keyword in keywords))
    
    # 작물 유형 특성
    for crop_category, crops in crop_types.items():
        features[f'crop_{crop_category}'] = int(any(crop in text for crop in crops))
    
    # 텍스트 길이 (상세도)
    features['text_length'] = len(text)
    
    # 괄호 안 정보 (작물명이 많이 들어감)
    bracket_content = re.findall(r'\(([^)]+)\)', text)
    features['has_bracket_info'] = int(len(bracket_content) > 0)
    
    return features

# 7. work_type 특성 추출
print("작업 유형 텍스트 분석 중...")
work_features_list = []
for work_type in df['work_type']:
    features = extract_work_features(work_type)
    work_features_list.append(features)

work_features_df = pd.DataFrame(work_features_list).fillna(0)
print(f"추출된 작업 특성 수: {len(work_features_df.columns)}")
print(f"주요 작업 특성:\n{work_features_df.sum().sort_values(ascending=False).head(10)}")

# 8. 지역 정보 처리
df['region_processed'] = df['region'].str.extract(r'\] ([^0-9\[\]]+?[시도군])', expand=False)
df['region_processed'] = df['region_processed'].fillna('기타지역')

# 지역 그룹핑
def categorize_region(region):
    if pd.isna(region) or region == '기타지역':
        return '기타지역'
    
    region = str(region)
    if '전북' in region or '전라북도' in region:
        return '전북'
    elif '전남' in region or '전라남도' in region:
        return '전남'
    elif '경북' in region or '경상북도' in region:
        return '경북'
    elif '경남' in region or '경상남도' in region:
        return '경남'
    elif '충북' in region or '충청북도' in region:
        return '충북'
    elif '충남' in region or '충청남도' in region:
        return '충남'
    else:
        return '기타지역'

df['region_category'] = df['region_processed'].apply(categorize_region)
print(f"\n지역 분포:\n{df['region_category'].value_counts()}")

# 9. 계절 정보
if 'season' in df.columns:
    df['season'] = df['season'].fillna('알수없음')
else:
    df['season'] = '알수없음'

# 10. 기본 피처 엔지니어링
df['total_count'] = df['male_count'] + df['female_count']
df['has_male'] = (df['male_count'] > 0).astype(int)
df['has_female'] = (df['female_count'] > 0).astype(int)
df['both_gender'] = ((df['male_count'] > 0) & (df['female_count'] > 0)).astype(int)

df['area_per_day'] = df['area_num'] / (df['duration_num'] + 1e-8)
df['log_area'] = np.log1p(df['area_num'])
df['log_duration'] = np.log1p(df['duration_num'])

# 범주형 변수
df['work_scale'] = pd.cut(df['total_count'], 
                         bins=[-1, 0, 2, 5, float('inf')], 
                         labels=['모집없음', '소규모', '중규모', '대규모']).astype(str)

df['duration_category'] = pd.cut(df['duration_num'], 
                               bins=[0, 1, 3, 7, float('inf')], 
                               labels=['당일', '단기', '중기', '장기']).astype(str)

# 11. 모든 특성 결합
numerical_features = [
    'area_num', 'duration_num', 'log_area', 'log_duration',
    'area_per_day', 'has_male', 'has_female', 'both_gender'
]

categorical_features = ['region_category', 'season', 'work_scale', 'duration_category']
work_feature_cols = work_features_df.columns.tolist()

# 결측치 처리
for col in categorical_features:
    if df[col].isna().any():
        df[col] = df[col].fillna('기타')
    df[col] = df[col].astype(str)

for col in numerical_features:
    if df[col].isna().any():
        df[col] = df[col].fillna(df[col].median())

# 12. 최종 특성 데이터 구성
X_basic = df[numerical_features + categorical_features].copy()
X_work = work_features_df.copy()
X = pd.concat([X_basic, X_work], axis=1)
y = df[['male_count','female_count','wage_male','wage_female']].copy()

print(f"\n최종 특성 구성:")
print(f"- 기본 수치 특성: {len(numerical_features)}개")
print(f"- 범주형 특성: {len(categorical_features)}개")
print(f"- 작업 유형 특성: {len(work_feature_cols)}개")
print(f"- 총 특성 수: {len(X.columns)}개")
print(f"- 데이터 크기: {len(X)}")

# 13. 전처리 파이프라인
preprocessor = ColumnTransformer([
    ('num', StandardScaler(), numerical_features),
    ('cat', OneHotEncoder(sparse_output=False, handle_unknown='ignore', drop='first'), categorical_features),
    ('work', 'passthrough', work_feature_cols)
])

# 14. 종합 예측 모델 클래스
class WorkforceWagePredictor:
    def __init__(self):
        self.count_models = {}
        self.wage_classifiers = {}
        self.wage_regressors = {}
        self.preprocessor = None
        self.wage_stats = {}
        
    def fit(self, X, y):
        self.preprocessor = preprocessor.fit(X)
        X_processed = self.preprocessor.transform(X)
        
        print(f"전처리 후 특성 수: {X_processed.shape[1]}")
        
        # 임금 통계 저장 (기본값 설정용)
        for target in ['wage_male', 'wage_female']:
            target_idx = ['male_count', 'female_count', 'wage_male', 'wage_female'].index(target)
            wage_values = y.iloc[:, target_idx]
            non_zero_wages = wage_values[wage_values > 0]
            
            self.wage_stats[target] = {
                'mean': non_zero_wages.mean() if len(non_zero_wages) > 0 else 100000,
                'median': non_zero_wages.median() if len(non_zero_wages) > 0 else 100000,
                'min': max(50000, non_zero_wages.min()) if len(non_zero_wages) > 0 else 80000,
                'max': non_zero_wages.max() if len(non_zero_wages) > 0 else 150000
            }
        
        # 1. 인원수 예측 모델
        for target in ['male_count', 'female_count']:
            model = RandomForestRegressor(
                n_estimators=200,
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
            
            # 임금 분류기 (0 vs >0) - 해당 성별 인원이 있을 때만
            valid_mask = count_values > 0
            if valid_mask.sum() > 0:
                wage_binary = (wage_values > 0).astype(int)
                
                if len(np.unique(wage_binary[valid_mask])) > 1:
                    classifier = LogisticRegression(random_state=42, max_iter=1000)
                    classifier.fit(X_processed[valid_mask], wage_binary[valid_mask])
                    self.wage_classifiers[target] = classifier
                else:
                    self.wage_classifiers[target] = None
            else:
                self.wage_classifiers[target] = None
            
            # 임금 회귀기 (>0인 경우)
            non_zero_mask = (wage_values > 0) & (count_values > 0)
            if non_zero_mask.sum() > 5:
                X_non_zero = X_processed[non_zero_mask]
                y_non_zero = wage_values[non_zero_mask]
                
                regressor = GradientBoostingRegressor(
                    n_estimators=150,
                    max_depth=8,
                    learning_rate=0.1,
                    random_state=42
                )
                regressor.fit(X_non_zero, y_non_zero)
                self.wage_regressors[target] = regressor
            else:
                self.wage_regressors[target] = None
                
        return self
    
    def predict(self, X):
        X_processed = self.preprocessor.transform(X)
        predictions = []
        
        # 1. 인원수 예측
        male_pred = self.count_models['male_count'].predict(X_processed)
        female_pred = self.count_models['female_count'].predict(X_processed)
        
        male_pred = np.maximum(0, male_pred)
        female_pred = np.maximum(0, female_pred)
        
        predictions.extend([male_pred, female_pred])
        
        # 2. 임금 예측
        for target in ['wage_male', 'wage_female']:
            count_pred = male_pred if target == 'wage_male' else female_pred
            wage_pred = np.zeros(len(X_processed))
            
            # 해당 성별 인원이 있는 경우에만 임금 예측
            has_count_mask = count_pred > 0.3  # 0.3 이상이면 인원이 있다고 판단
            
            if has_count_mask.sum() > 0:
                X_with_count = X_processed[has_count_mask]
                
                # 임금 분류 (임금이 있을지 없을지)
                if self.wage_classifiers[target] is not None:
                    wage_prob = self.wage_classifiers[target].predict_proba(X_with_count)
                    if wage_prob.shape[1] > 1:
                        has_wage_prob = wage_prob[:, 1]  # 임금이 있을 확률
                    else:
                        has_wage_prob = np.ones(len(X_with_count))
                else:
                    has_wage_prob = np.ones(len(X_with_count))
                
                # 임금이 있을 확률이 0.3 이상인 경우 임금 예측
                wage_mask = has_wage_prob > 0.3
                
                if wage_mask.sum() > 0 and self.wage_regressors[target] is not None:
                    # 임금 회귀 예측
                    wage_values = self.wage_regressors[target].predict(X_with_count[wage_mask])
                    
                    # 최소/최대 임금 제한
                    wage_values = np.clip(wage_values, 
                                        self.wage_stats[target]['min'], 
                                        self.wage_stats[target]['max'])
                    
                    # 결과 저장
                    temp_wages = np.zeros(has_count_mask.sum())
                    temp_wages[wage_mask] = wage_values
                    wage_pred[has_count_mask] = temp_wages
                    
                elif has_count_mask.sum() > 0:
                    # 회귀 모델이 없으면 평균값 사용
                    default_wage = self.wage_stats[target]['median']
                    wage_pred[has_count_mask] = default_wage * has_wage_prob
            
            predictions.append(wage_pred)
        
        return np.column_stack(predictions)
    
    def get_feature_importance(self, target='male_count'):
        if target in self.count_models:
            model = self.count_models[target]
            if hasattr(model, 'feature_importances_'):
                return model.feature_importances_
        return None

# 15. 데이터 분할
X_train, X_test, y_train, y_test = train_test_split(
    X, y, test_size=0.2, random_state=42
)

print(f"\n훈련 데이터: {len(X_train)}, 테스트 데이터: {len(X_test)}")

# 16. 모델 학습
print("\n인력 및 임금 통합 예측 모델 학습 중...")
model = WorkforceWagePredictor()
model.fit(X_train, y_train)

# 17. 예측 및 평가
y_pred = model.predict(X_test)

target_names = ['male_count', 'female_count', 'wage_male', 'wage_female']
print("\n=== 통합 예측 모델 성능 ===")
for i, target in enumerate(target_names):
    actual = y_test.iloc[:, i]
    predicted = y_pred[:, i]
    
    mse = mean_squared_error(actual, predicted)
    mae = mean_absolute_error(actual, predicted)
    r2 = r2_score(actual, predicted)
    
    if 'wage' in target:
        # 임금의 경우 0값 정확도도 중요
        zero_actual = (actual == 0)
        zero_predicted = (predicted < 1000)  # 1000원 미만은 0으로 간주
        zero_accuracy = (zero_actual == zero_predicted).mean()
        
        # 0이 아닌 값들에 대한 정확도
        non_zero_mask = actual > 0
        if non_zero_mask.sum() > 0:
            non_zero_mae = mean_absolute_error(actual[non_zero_mask], predicted[non_zero_mask])
            print(f"{target}: MAE={mae:.0f}, RMSE={np.sqrt(mse):.0f}, R²={r2:.3f}, 0값정확도={zero_accuracy:.3f}, 비0값MAE={non_zero_mae:.0f}")
        else:
            print(f"{target}: MAE={mae:.0f}, RMSE={np.sqrt(mse):.0f}, R²={r2:.3f}, 0값정확도={zero_accuracy:.3f}")
    else:
        print(f"{target}: MAE={mae:.2f}, RMSE={np.sqrt(mse):.2f}, R²={r2:.3f}")

# 18. 모델 저장
joblib.dump(model, "ai/models/work_type_aware_model.pkl")

# 19. 예측 테스트
print("\n=== 예측 테스트 ===")
sample_size = min(10, len(X_test))

for i in range(sample_size):
    original_work_type = df.iloc[X_test.index[i]]['work_type']
    
    sample_pred = model.predict(X_test.iloc[i:i+1])
    sample_actual = y_test.iloc[i]
    
    print(f"\n샘플 {i+1}:")
    print(f"작업: {original_work_type}")
    print(f"실제값: 남{int(sample_actual['male_count'])}, 여{int(sample_actual['female_count'])}, 남임금{int(sample_actual['wage_male'])}, 여임금{int(sample_actual['wage_female'])}")
    print(f"예측값: 남{sample_pred[0][0]:.0f}, 여{sample_pred[0][1]:.0f}, 남임금{sample_pred[0][2]:.0f}, 여임금{sample_pred[0][3]:.0f}")

print("\n모델 학습 완료!")