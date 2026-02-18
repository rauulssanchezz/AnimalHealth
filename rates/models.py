import uuid
from django.db import models
from django.core.validators import MinValueValidator, MaxValueValidator
from clinics.models import Clinic
from users.models import User

class Rate(models.Model):
    id = models.UUIDField(
        primary_key=True, 
        default=uuid.uuid4, 
        editable=False
    )

    rate = models.IntegerField(
        validators=[MinValueValidator(0),MaxValueValidator(5)]
    )

    clinic = models.ForeignKey(Clinic, related_name='rates', on_delete=models.CASCADE)
    user = models.ForeignKey(User, related_name='rates', on_delete=models.CASCADE)

    comment = models.TextField(null=True)

    class Meta:
        constraints = [
            models.CheckConstraint(
                condition=models.Q(rate__gte=0, rate__lte=5), 
                name='rate_range_0_5'
            ),
        ]
        unique_together = ('user', 'clinic')
