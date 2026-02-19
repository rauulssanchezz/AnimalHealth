from rest_framework import serializers
from pets.models import Pet
from users.models import User

ID = 'id'
CHIP = 'chip'
OWNER = 'owner'
NAME = 'name'
INFORMATION = 'information'
OWNER_NAME = 'owner_name'
IMAGE = 'image'
RACE = 'race'
SPECIES = 'species'
OWNER_ID = 'owner_id'

class PetBaseSerializer(serializers.ModelSerializer):
    owner = serializers.HiddenField(default=serializers.CurrentUserDefault())
    owner_name = serializers.ReadOnlyField(source='owner.username')
    owner_id = serializers.ReadOnlyField(source='owner.id')

    class Meta:
        model = Pet
        fields = [ID, CHIP, OWNER, NAME, INFORMATION, OWNER_NAME, IMAGE, SPECIES, RACE, OWNER_ID]
        read_only_fields = [ID, OWNER_NAME, OWNER_ID]

class PetVetSerializer(PetBaseSerializer):
    owner = serializers.PrimaryKeyRelatedField(
        queryset=User.objects.all()
    )