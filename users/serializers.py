from rest_framework import serializers
from django.contrib.auth import get_user_model

User = get_user_model()

EMAIL = 'email'
USERNAME = 'username'
PASSWORD = 'password'
IS_VET = 'is_vet'
CLINIC_ADMIN = 'clinic_admin'
WORKS_AT = 'works_at'

class UserPublicSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = [EMAIL, USERNAME]
        read_only = [EMAIL, USERNAME]

class VetPublicSerializer(serializers.ModelSerializer):
    class Meta:
        model = User
        fields = [IS_VET, CLINIC_ADMIN, WORKS_AT]
        read_only = [IS_VET, CLINIC_ADMIN, WORKS_AT]

class RegisterSerializer(serializers.ModelSerializer):
    password = serializers.CharField(write_only = True)

    class Meta:
        model = User
        fields = [EMAIL, USERNAME, PASSWORD, IS_VET, CLINIC_ADMIN, WORKS_AT]
        extra_kwargs = {
            'password': {'write_only': True},
            'email': {'required': True}
        }

    def validate(self, data):
        is_vet = data.get(IS_VET, False)
        works_at = data.get(WORKS_AT, '')
        clinic_admin = data.get(CLINIC_ADMIN, False)

        if is_vet and not works_at and not clinic_admin:
            raise serializers.ValidationError({
                WORKS_AT: "Los usuarios marcados como veterinarios deben estar asociados a una clínica."
            })
            
        if not is_vet and works_at:
            data[WORKS_AT] = None

        return data

    def create(self, validated_data):
        password = validated_data.pop(PASSWORD)
        
        return User.objects.create_user(password=password, **validated_data)
    
    def update(self, instance, validated_data):
        password = validated_data.pop(PASSWORD, None)
        if password:
            instance.set_password(password)
        
        return super().update(instance, validated_data)