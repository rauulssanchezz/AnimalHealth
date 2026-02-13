from django.contrib import admin
from django.urls import path
from users.views import RegisterView
from rest_framework.authtoken import views as auth_views

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/register/', RegisterView.as_view(), name='register'),
    path('api/login/', auth_views.obtain_auth_token, name='login')
]
