import uuid
from django.db import models
from users.models import User
from django.core.validators import RegexValidator

chip_validator = RegexValidator(
    regex=r'^\d{15}$',
    message='El número de chip debe tener exactamente 15 dígitos numéricos.'
)

class Species(models.TextChoices):
        CANINE = 'CANINE', 'Canino'
        FELINE = 'FELINE', 'Felino'
        RABBIT = 'RABBIT', 'Conejo'
        OTHER = 'OTHER', 'Otro'

class Pet(models.Model):
    id = models.UUIDField(
        primary_key=True, 
        default=uuid.uuid4, 
        editable=False
    )

    chip = models.CharField(
        unique=True,
        max_length=15,
        validators=[chip_validator],
        help_text="Código ISO de 15 dígitos del microchip"
    )
    owner = models.ForeignKey(User, related_name='pets', on_delete=models.CASCADE)
    name = models.CharField()
    information = models.TextField()
    species = models.CharField(
        max_length=20,
        choices=Species.choices,
        default=Species.CANINE
    )
    race = models.CharField()
    image = models.ImageField(upload_to='pets/', null=True)

