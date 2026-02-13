from geopy.geocoders import Nominatim
from rest_framework import serializers
from clinics.models import Clinic, ClinicImage

EMAIL = 'email'
NAME = 'name'
ADDRESS = 'address'
LATITUDE = 'latitude'
LONGITUDE = 'longitude'
CLINIC = 'clinic'
UPLOADED_IMAGES = 'uploaded_images'

geolocator = Nominatim(user_agent="animal_health")

class ClinicSerializer(serializers.ModelSerializer):
    uploaded_images = serializers.ListField(
        child=serializers.ImageField(max_length=1000000, allow_empty_file=False, use_url=False),
        write_only=True,
        required=False
    )
    
    class Meta:
        model = Clinic
        fields = [EMAIL, NAME, ADDRESS, UPLOADED_IMAGES]
        read_only_fields = [LATITUDE, LONGITUDE]

    def create(self, validated_data):
        address = validated_data[ADDRESS]
        location = geolocator.geocode(address)
        images_data = validated_data.pop(UPLOADED_IMAGES, [])

        try:
            location = geolocator.geocode(address)
        except Exception:
            raise serializers.ValidationError({
                'address': 'El servicio de geolocalización no responde.'
            })

        if not location:
            raise serializers.ValidationError({
                'address': 'La dirección proporcionada no existe o es inválida.'
            })
        
        validated_data[LATITUDE] = location.latitude
        validated_data[LONGITUDE] = location.longitude

        clinic = Clinic.objects.create(**validated_data)

        for image in images_data:
            ClinicImage.objects.create(clinic=clinic, image=image)

        return clinic