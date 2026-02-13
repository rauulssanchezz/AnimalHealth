import uuid
from django.db import models
from users.models import User

class Chat(models.Model):
    id = models.UUIDField(
        primary_key=True, 
        default=uuid.uuid4, 
        editable=False
    )

    client = models.ForeignKey(User, related_name='user_chat', null=False, blank=False, on_delete=models.CASCADE)
    vet = models.ForeignKey(User, related_name='vet_chat', null=False, blank=False, on_delete=models.CASCADE)
    created_at = models.DateTimeField(auto_now_add=True)

    class Meta:
        verbose_name = "Chat"
        unique_together = ['client', 'vet']
