from rest_framework import serializers
from django.contrib.auth import get_user_model

User = get_user_model()

EMAIL = 'email'
USERNAME = 'username'
PASSWORD = 'password'
IS_VET = 'is_vet'
CLINIC = 'clinic'
CLINIC_ADMIN = 'clinic_admin'

class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only = True)

    class Meta:
        model = User
        fields = [EMAIL, USERNAME, PASSWORD, IS_VET, CLINIC, CLINIC_ADMIN]

    def create(self, validated_data):
        return User.objects.create_user(
            email=validated_data[EMAIL],
            username=validated_data[USERNAME],
            password=validated_data[PASSWORD],
            IS_VET=validated_data[IS_VET],
            CLINIC=validated_data[CLINIC],
            CLINIC_ADMIN=validated_data[CLINIC_ADMIN]
        )