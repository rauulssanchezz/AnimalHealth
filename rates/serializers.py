from rest_framework import serializers
from rest_framework.validators import UniqueTogetherValidator
from .models import Rate

class RatesSerializer(serializers.ModelSerializer):
    user = serializers.HiddenField(default=serializers.CurrentUserDefault())
    user_name = serializers.ReadOnlyField(source='user.username')

    class Meta:
        model = Rate
        fields = ['id', 'clinic', 'rate', 'user', 'comment', 'user_name']

        validators = [
            UniqueTogetherValidator(
                queryset=Rate.objects.all(),
                fields=['user', 'clinic'],
                message="Ya has dejado una valoración para esta clínica."
            )
        ]