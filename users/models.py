import uuid
from django.db import models
from django.contrib.auth.models import AbstractUser
from clinics.models import Clinic

class User(AbstractUser):
    id = models.UUIDField(primary_key=True, default=uuid.uuid4, editable=False)
    email = models.EmailField(unique=True)
    username = models.CharField(max_length=150, unique=False, blank=True, null=True)
    is_vet = models.BooleanField(default=False, blank=True, null=False)
    clinic_admin=models.BooleanField(default=False, blank=True, null=False)
    clinic=models.ForeignKey(Clinic, related_name='vets', on_delete=models.CASCADE, null=True, blank=True)

    USERNAME_FIELD = 'email'

    REQUIRED_FIELDS = ['username']

    def __str__(self) :
        return self.email
