import uuid
from django.db import models
from users.models import User

class Clinic(models.Model):
    id = models.UUIDField(
        primary_key=True, 
        default=uuid.uuid4, 
        editable=False
    )
    name = models.CharField(max_length=255)
    address = models.CharField(max_length=500)
    latitude = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)
    longitude = models.DecimalField(max_digits=9, decimal_places=6, null=True, blank=True)
    admin = models.ForeignKey(User, related_name='clinic', on_delete=models.CASCADE)
    email = models.EmailField()

    def __str__(self):
        return self.name
    
class ClinicImage(models.Model):
    clinic = models.ForeignKey(Clinic, related_name='images', on_delete=models.CASCADE)
    image = models.ImageField(upload_to='clinics/')
    created_at = models.DateTimeField(auto_now_add=True)