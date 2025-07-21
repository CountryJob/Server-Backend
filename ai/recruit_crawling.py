import requests
from bs4 import BeautifulSoup
import re
from datetime import datetime
import pandas as pd
import time
from urllib.parse import urljoin, parse_qs, urlparse

class IlmoaCrawler:
    def __init__(self):
        self.base_url = "https://www.ilmoa.kr/main"
        self.session = requests.Session()
        self.session.headers.update({
            'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36',
            'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8',
            'Accept-Language': 'ko-KR,ko;q=0.8,en-US;q=0.5,en;q=0.3',
            'Accept-Encoding': 'gzip, deflate, br',
            'Connection': 'keep-alive',
            'Upgrade-Insecure-Requests': '1'
        })
        
    def extract_employ_id(self, href):
        """URL에서 employ_id 추출"""
        try:
            parsed_url = urlparse(href)
            params = parse_qs(parsed_url.query)
            return params.get('employ_id', [None])[0]
        except:
            # 정규식으로 백업 추출
            match = re.search(r'employ_id=([A-Za-z0-9\-._]+)', href)
            return match.group(1) if match else None

    def parse_detail_page(self, employ_id):
        """상세 페이지에서 작업시간, 위치, 모집인원, 임금조건 추출"""
        work_time = ''
        location = ''
        total_count = male_count = female_count = 0
        wage_male = wage_female = 0
        
        try:
            url = f"{self.base_url}?gc=EMPLOY&do=view&employ_id={employ_id}"
            response = self.session.get(url, timeout=15)
            response.raise_for_status()
            response.encoding = 'utf-8'
            
            soup = BeautifulSoup(response.text, 'html.parser')
            dl_list = soup.select('.work_view .list dl')
            
            for dl in dl_list:
                key = dl.select_one('dt')
                val = dl.select_one('dd')
                if not key or not val:
                    continue
                key_text = key.get_text(strip=True)
                val_text = val.get_text(" ", strip=True)

                if '작업시간' in key_text:
                    work_time = val_text
                elif '작업장위치' in key_text:
                    location = val_text
                elif '모집인원' in key_text:
                    # 정규식보다 태그 기반으로 숫자 추출
                    bs = val.find_all("b")
                    if len(bs) >= 3:
                        total_count = int(bs[0].text)
                        male_count = int(bs[1].text)
                        female_count = int(bs[2].text)
                elif '임금조건' in key_text:
                    bs = val.find_all("b")
                    if '남' in val_text and len(bs) >= 1:
                        wage_male = int(bs[0].text.replace(',', ''))
                    if '여' in val_text and len(bs) >= 2:
                        wage_female = int(bs[1].text.replace(',', ''))
                        
        except Exception as e:
            print(f"상세 페이지 파싱 오류 (employ_id: {employ_id}): {e}")

        return work_time, location, total_count, male_count, female_count, wage_male, wage_female


    def parse_salary(self, pay_text):
        """급여 정보 파싱"""
        wage_male = 0
        wage_female = 0
        
        # 다양한 패턴으로 급여 추출
        patterns = [
            r'남[:\s]*(\d{1,3}(?:,\d{3})*)',
            r'여[:\s]*(\d{1,3}(?:,\d{3})*)',
            r'남자[:\s]*(\d{1,3}(?:,\d{3})*)',
            r'여자[:\s]*(\d{1,3}(?:,\d{3})*)'
        ]
        
        male_match = re.search(patterns[0] + '|' + patterns[2], pay_text)
        female_match = re.search(patterns[1] + '|' + patterns[3], pay_text)
        
        if male_match:
            wage_male = int(male_match.group(1).replace(',', ''))
        if female_match:
            wage_female = int(female_match.group(1).replace(',', ''))
            
        # 성별 구분 없이 급여가 하나만 있는 경우
        if not wage_male and not wage_female:
            general_match = re.search(r'(\d{1,3}(?:,\d{3})*)', pay_text)
            if general_match:
                salary = int(general_match.group(1).replace(',', ''))
                if '여' in pay_text:
                    wage_female = salary
                elif '남' in pay_text:
                    wage_male = salary
                else:
                    wage_male = wage_female = salary
                    
        return wage_male, wage_female

    def parse_recruit_info(self, recruit_text):
        """모집 정보 파싱"""
        total_count = 0
        male_count = 0
        female_count = 0
        
        # 총 인원수 추출
        total_match = re.search(r'(\d+)명', recruit_text)
        if total_match:
            total_count = int(total_match.group(1))
            
        # 성별 인원수 추출
        male_match = re.search(r'남[:\s]*(\d+)', recruit_text)
        female_match = re.search(r'여[:\s]*(\d+)', recruit_text)
        
        if male_match:
            male_count = int(male_match.group(1))
        if female_match:
            female_count = int(female_match.group(1))
            
        # 성별 구분이 없으면 전체를 남녀로 균등 분배
        if male_count == 0 and female_count == 0 and total_count > 0:
            if '여' in recruit_text and '남' not in recruit_text:
                female_count = total_count
            elif '남' in recruit_text and '여' not in recruit_text:
                male_count = total_count
            else:
                male_count = total_count // 2
                female_count = total_count - male_count
                
        return total_count, male_count, female_count

    def calculate_work_info(self, period_text, work_time):
        """작업 기간 및 시급 계산"""
        duration = 1
        season = ''
        hourly_wage = '계산불가'
        
        try:
            # 작업 기간 계산
            if '~' in period_text:
                start_str, end_str = [d.strip() for d in period_text.split('~')]
                start_date = datetime.strptime(start_str, '%Y-%m-%d')
                end_date = datetime.strptime(end_str, '%Y-%m-%d')
                duration = (end_date - start_date).days + 1
                
                # 계절 판단
                month = start_date.month
                if month in [12, 1, 2]:
                    season = '겨울'
                elif month in [3, 4, 5]:
                    season = '봄'
                elif month in [6, 7, 8]:
                    season = '여름'
                else:
                    season = '가을'
        except Exception as e:
            print(f"기간 계산 오류: {e}")
            
        return duration, season

    def calculate_hourly_wage(self, work_time, daily_wage):
        """시급 계산"""
        try:
            if '~' in work_time and daily_wage > 0:
                time_parts = work_time.replace(' ', '').split('~')
                start_time = datetime.strptime(time_parts[0], '%H:%M')
                end_time = datetime.strptime(time_parts[1], '%H:%M')
                work_hours = (end_time - start_time).seconds / 3600
                
                if work_hours > 0:
                    return f"{int(daily_wage / work_hours)}원"
        except Exception as e:
            print(f"시급 계산 오류: {e}")
            
        return '계산불가'

    def crawl_jobs(self, max_pages=30, delay=1):
        """메인 크롤링 함수"""
        all_jobs = []
        
        for page in range(1, max_pages + 1):
            print(f"페이지 {page} 크롤링 중...")
            
            try:
                response = self.session.get(
                    f"{self.base_url}?gc=EMPLOY&page={page}", 
                    timeout=15
                )
                response.raise_for_status()
                response.encoding = 'utf-8'
                
                soup = BeautifulSoup(response.text, 'html.parser')
                
                # 다양한 선택자로 job 카드 찾기
                job_cards = (
                    soup.select('tr.center') or 
                    soup.select('.job-card') or 
                    soup.select('td.mobile.left') or
                    soup.select('.job-item')
                )
                
                if not job_cards:
                    print(f"페이지 {page}에서 일자리를 찾을 수 없습니다.")
                    continue
                
                for card in job_cards:
                    job_info = self.parse_job_card(card)
                    if job_info:
                        all_jobs.append(job_info)
                        
                print(f"페이지 {page}: {len([j for j in all_jobs if j])} 개 일자리 수집")
                
                # 딜레이
                if delay > 0:
                    time.sleep(delay)
                    
            except Exception as e:
                print(f"페이지 {page} 크롤링 오류: {e}")
                continue
                
        return [job for job in all_jobs if job]  # None 값 제거

    def parse_job_card(self, card):
        """개별 job 카드 파싱"""
        try:
            # employ_id 추출
            link = card.select_one('a[href*="employ_id"], .Fix_ListBtns[href*="employ_id"]')
            if not link:
                return None
                
            employ_id = self.extract_employ_id(link['href'])
            if not employ_id:
                return None
            
            # 기본 정보 추출
            farm_name_elem = card.select_one('.stit p, .farm-name, h3, .title')
            job_title_elem = card.select_one('.btit, .job-title, h4, .subtitle')
            
            if not farm_name_elem or not job_title_elem:
                return None
                
            farm_name = farm_name_elem.get_text(strip=True)
            job_title = job_title_elem.get_text(strip=True)
            
            # 모집 정보
            recruit_elem = card.select_one('li:contains("모집"), .recruit-info')
            recruit_text = recruit_elem.get_text(strip=True) if recruit_elem else ""
            total_count, male_count, female_count = self.parse_recruit_info(recruit_text)
            
            # 지역 정보
            region_elem = card.select_one('li:contains("지역"), li:contains("근무지역"), .region')
            region = region_elem.get_text(strip=True).replace('근무지역 :', '').strip() if region_elem else ""
            
            # 기간 정보
            period_elem = card.select_one('li:contains("기간"), li:contains("작업기간"), .period')
            period = period_elem.get_text(strip=True).replace('작업기간 :', '').strip() if period_elem else ""
            
            # 급여 정보
            pay_elem = card.select_one('.pay, .salary, li:contains("급여")')
            pay_text = pay_elem.get_text(" ", strip=True) if pay_elem else ""
            wage_male, wage_female = self.parse_salary(pay_text)
            
            # 상세 정보 가져오기
            work_time, location, total_count, male_count, female_count, wage_male, wage_female = self.parse_detail_page(employ_id)
            
            # 계산
            duration, season = self.calculate_work_info(period, work_time)
            avg_wage = (wage_male + wage_female) // 2 if wage_male and wage_female else wage_male or wage_female
            hourly_wage = self.calculate_hourly_wage(work_time, avg_wage)
            
            return {
                "농가명": farm_name,
                "작업 종류": job_title,
                "모집 인원": f"총 {total_count}명 / 남 : {male_count}명 / 여 : {female_count}명",
                "보수 (총)": f"남 {wage_male:,} 원 / 여 {wage_female:,} 원",
                "작업기간": period,
                "기간(일)": duration,
                "작업시간": work_time or "정보없음",
                "임금조건(남)": f"{wage_male:,}원",
                "임금조건(여)": f"{wage_female:,}원",
                "평균 일급": f"{avg_wage:,}원",
                "보수(시급)": hourly_wage,
                "지역": location or region,
                "계절": season,
                "employ_id": employ_id
            }
            
        except Exception as e:
            print(f"카드 파싱 오류: {e}")
            return None

def main():
    """메인 실행 함수"""
    crawler = IlmoaCrawler()
    
    print("일모아 일자리 크롤링을 시작합니다...")
    jobs = crawler.crawl_jobs(max_pages=30, delay=1)
    
    if jobs:
        # DataFrame 생성 및 중복 제거
        df = pd.DataFrame(jobs)
        df = df.drop_duplicates('employ_id').reset_index(drop=True)
        
        # CSV 저장
        filename = "ai/data/ilmoa_jobs2.csv"
        df.to_csv(filename, index=False, encoding="utf-8-sig")
        
        print(f"\n크롤링 완료!")
        print(f"총 {len(df)}개 일자리 정보 수집")
        print(f"CSV 파일 저장: {filename}")
        
        # 샘플 데이터 출력
        if len(df) > 0:
            print("\n=== 샘플 데이터 ===")
            for key, value in df.iloc[0].items():
                if key != 'employ_id':
                    print(f"{key}: {value}")
    else:
        print("수집된 일자리 정보가 없습니다.")

if __name__ == "__main__":
    main()