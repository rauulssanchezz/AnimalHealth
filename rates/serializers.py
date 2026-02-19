from rest_framework import serializers
from rest_framework.validators import UniqueTogetherValidator
from .models import Rate

ID = 'id'
CLINIC = 'clinic'
RATE = 'rate'
USER = 'user'
COMMENT = 'comment'
USER_NAME = 'user_name'

class RatesSerializer(serializers.ModelSerializer):
    user = serializers.HiddenField(default=serializers.CurrentUserDefault())
    user_name = serializers.ReadOnlyField(source='user.username')

    class Meta:
        model = Rate
        fields = [ID, CLINIC, RATE, USER, COMMENT, USER_NAME]

        validators = [
            UniqueTogetherValidator(
                queryset=Rate.objects.all(),
                fields=[USER, CLINIC],
                message="Ya has dejado una valoración para esta clínica."
            )
        ]