from rest_framework import serializers
from .models import Chat

ID = 'id'
CLIENT = 'client'
VET = 'vet'

class CreateChatSerializer(serializers.ModelSerializer):
    client = serializers.HiddenField(default=serializers.CurrentUserDefault())
    class Meta:
        model = Chat
        fields = [ID, CLIENT, VET]
        read_only_fields = [ID]

    def validate(self, data):
        if data['client'] == data['vet']:
            raise serializers.ValidationError(
                "Un usuario no puede iniciar un chat consigo mismo."
            )
        return data