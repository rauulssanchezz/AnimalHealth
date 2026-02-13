from rest_framework import serializers
from django.contrib.auth import get_user_model

User = get_user_model()

EMAIL = 'email'
USERNAME = 'username'
PASSWORD = 'password'

class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only = True)

    class Meta:
        model = User
        fields = [EMAIL, USERNAME, PASSWORD]

    def createUser(self, validated_data):
        user = User.objects.create_user(
            email=validated_data[EMAIL],
            username=validated_data[USERNAME],
            password=validated_data[PASSWORD]
        )

        return user