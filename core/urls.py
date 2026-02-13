from django.contrib import admin
from django.urls import path
from users.views import RegisterView
from clinics.views import CreateClinicView
from rest_framework.authtoken import views as auth_views

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/auth/register/', RegisterView.as_view(), name='register'),
    path('api/auth/login/', auth_views.obtain_auth_token, name='login'),
    path('api/clinic/create', CreateClinicView.as_view(), name='create_clinic')
]
