from django.http import HttpResponse
from django.shortcuts import get_list_or_404
from .models import Restaurant
from selenium import webdriver
from selenium.webdriver.common.by import By
from selenium.webdriver.common.keys import Keys
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from selenium.common.exceptions import TimeoutException, ElementClickInterceptedException
from webdriver_manager.chrome import ChromeDriverManager
import time

def crawling(request):
    if request.method == 'POST':  # ✅ POST 요청만 허용
        restaurants = Restaurant.objects.all()  # ✅ 모든 Restaurant 객체 가져오기

        options = webdriver.ChromeOptions()
        options.add_argument("--start-maximized")  # 창 최대화
        options.add_argument("--disable-blink-features=AutomationControlled")  # 자동화 탐지 방지

        driver = webdriver.Chrome(service=Service(ChromeDriverManager().install()), options=options)

        for restaurant in restaurants:
            print(f"🔍 검색 중: {restaurant.name}")

            # ✅ 네이버 지도 열기
            driver.get("https://map.naver.com/v5/")
            time.sleep(3)  # 초기 로딩 대기

            # ✅ 검색어 입력
            try:
                search_box = WebDriverWait(driver, 10).until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, "input.input_search"))
                )
                search_box.send_keys(restaurant.name)
                search_box.send_keys(Keys.ENTER)
            except TimeoutException:
                print(f"❌ 검색창을 찾을 수 없음: {restaurant.name}")
                continue  # 다음 레스토랑으로 넘어감

            time.sleep(3)  # 검색 결과 로딩 대기
            driver.switch_to.default_content()

            try:
                # ✅ `entryIframe`이 이미 떠 있는 경우
                entry_iframe = WebDriverWait(driver, 3).until(
                    EC.presence_of_element_located((By.CSS_SELECTOR, "iframe#entryIframe"))
                )
                print(f"⚠️ entryIframe이 이미 떠 있음: {restaurant.name}")
                place_url = driver.current_url  # ✅ 현재 페이지의 URL 저장
            except TimeoutException:
                print(f"✅ entryIframe이 없음, 검색 결과에서 찾아볼게요: {restaurant.name}")

                try:
                    # ✅ `searchIframe`으로 이동 (검색 결과 리스트에서 선택)
                    search_iframe = WebDriverWait(driver, 10).until(
                        EC.presence_of_element_located((By.CSS_SELECTOR, "iframe#searchIframe"))
                    )
                    driver.switch_to.frame(search_iframe)

                    first_result = WebDriverWait(driver, 10).until(
                        EC.presence_of_element_located((By.CSS_SELECTOR, "div.ouxiq"))
                    )
                    first_result.click()  # ✅ 첫 번째 검색 결과 클릭

                    driver.switch_to.default_content()

                    # ✅ 상세 페이지 iframe 전환
                    entry_iframe = WebDriverWait(driver, 10).until(
                        EC.presence_of_element_located((By.CSS_SELECTOR, "iframe#entryIframe"))
                    )
                    driver.switch_to.frame(entry_iframe)
                    place_url = driver.current_url

                except (TimeoutException, ElementClickInterceptedException):
                    print(f"❌ 검색 결과를 찾지 못함: {restaurant.name}")
                    place_url = "URL 없음"

            print(f"✅ 저장된 URL: {place_url}")

            # ✅ `Restaurant` 모델에 URL 저장
            restaurant.map_url = place_url
            restaurant.save()

        driver.quit()  # ✅ 브라우저 닫기

        return HttpResponse("✅ 모든 식당 크롤링 완료!")

    return HttpResponse("❌ 잘못된 요청입니다.", status=400)
