from django.db import models

# Create your models here.


class Restaurant(models.Model):
    """
    식당 모델
    """
    id = models.AutoField(primary_key=True)  # Django는 기본적으로 id 필드를 자동 생성
    name = models.CharField(max_length=255)  # 이름
    category = models.CharField(max_length=255)  # 카테고리
    road_address = models.CharField(max_length=255)  # 도로명 주소

    search_query = models.CharField(max_length=255)  # 검색 쿼리 (유니크)

    mapx = models.FloatField()  # 지도 X 좌표
    mapy = models.FloatField()  # 지도 Y 좌표

    station = models.CharField(max_length=255, blank=True, null=True)  # 가까운 지하철역 (nullable)

    map_url = models.URLField(blank=True, null=True)  # 네이버 지도 URL (nullable)

    class Meta:
        db_table = "restaurant"

    def __str__(self):
        return self.name
