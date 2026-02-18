from rest_framework import serializers
from django.contrib.auth import get_user_model

User = get_user_model()

EMAIL = 'email'
USERNAME = 'username'
PASSWORD = 'password'
IS_VET = 'is_vet'
CLINIC_ADMIN = 'clinic_admin'

class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only = True)

    class Meta:
        model = User
        fields = [EMAIL, USERNAME, PASSWORD, IS_VET, CLINIC_ADMIN]
        extra_kwargs = {
            'password': {'write_only': True},
            'email': {'required': True}
        }

    def create(self, validated_data):
        password = validated_data.pop(PASSWORD)
        
        return User.objects.create_user(password=password, **validated_data)
    
    def update(self, instance, validated_data):
        password = validated_data.pop(PASSWORD, None)
        if password:
            instance.set_password(password)
        
        return super().update(instance, validated_data)