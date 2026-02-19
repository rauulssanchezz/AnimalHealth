from django.contrib import admin
from django.urls import include, path
from users.views import LogoutView, RegisterView, UserInformationView, UserProfileView, VetViewSet
from clinics.views import ClinicViewSet
from rest_framework.authtoken import views as auth_views
from rest_framework.routers import DefaultRouter
from rates.views import RateViewSet
from pets.views import PetViewSet

router = DefaultRouter()

router.register(r'clinics', ClinicViewSet, basename='clinic')
router.register(r'rates', RateViewSet, basename='rate')
router.register(r'vets', VetViewSet, basename='vets')
router.register(r'pets', PetViewSet, basename='pets')

urlpatterns = [
    path('admin/', admin.site.urls),
    path('api/auth/register/', RegisterView.as_view(), name='register'),
    path('api/auth/login/', auth_views.obtain_auth_token, name='login'),
    path('api/users/me/', UserProfileView.as_view(), name='user-profile'),
    path('api/auth/logout/', LogoutView.as_view(), name='logout'),
    path('api/users/<uuid:pk>/', UserInformationView.as_view(), name='user-information'),
    path('api/', include(router.urls)),
]
