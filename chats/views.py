from rest_framework import viewsets, permissions
from django.db import models
from .models import Chat

class CreateChatView(viewsets.ViewSet):
    permission_classes=[permissions.IsAuthenticated]

    def get_queryset(self):
        user = self.request.user
        return Chat.objects.filter(
            models.Q(client=user) | models.Q(vet=user)
        ).order_by('-created_at')
