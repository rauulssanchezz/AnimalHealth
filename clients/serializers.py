from rest_framework import serializers
from clients.models import Client

USER = 'user'
CLINIC = 'clinic'

class ClientSerializer(serializers.ModelSerializer):
    class Meta:
        model = Client
        fields = [USER, CLINIC] 