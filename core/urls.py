from django.contrib import admin
from django.urls import include, path
from users.views import RegisterView
from clinics.views import ClinicView
from rest_framework.authtoken import views as auth_views
from rest_framework.routers import DefaultRouter

router = DefaultRouter()

router.register(r'clinics', ClinicView, basename='clinic')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/auth/register/', RegisterView.as_view(), name='register'),
    path('api/auth/login/', auth_views.obtain_auth_token, name='login'),
    path('api/', include(router.urls)),
]
