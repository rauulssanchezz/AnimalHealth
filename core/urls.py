from django.contrib import admin
from django.urls import include, path
from users.views import LogoutView, RegisterView, UserProfileView
from clinics.views import ClinicViewSet
from rest_framework.authtoken import views as auth_views
from rest_framework.routers import DefaultRouter
from rates.views import RateViewSet

router = DefaultRouter()

router.register(r'clinics', ClinicViewSet, basename='clinic')
router.register(r'rates', RateViewSet, basename='rate')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/auth/register/', RegisterView.as_view(), name='register'),
    path('api/auth/login/', auth_views.obtain_auth_token, name='login'),
    path('api/auth/me', UserProfileView.as_view(), name='user-profile'),
    path('api/auth/logout', LogoutView.as_view(), name='logout'),
    path('api/', include(router.urls)),
]
