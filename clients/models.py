from django.db import models
from users.models import User
from clinics.models import Clinic

class Client(models.Model):
    user = models.ForeignKey(User, related_name='clinics', on_delete=models.CASCADE)
    clinic = models.ForeignKey(Clinic, related_name='clients', on_delete=models.CASCADE)
