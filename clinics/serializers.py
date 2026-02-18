from geopy.geocoders import Nominatim
from rest_framework import serializers
from clinics.models import Clinic, ClinicImage
from rates.serializers import RatesSerializer

EMAIL = 'email'
NAME = 'name'
ADDRESS = 'address'
LATITUDE = 'latitude'
LONGITUDE = 'longitude'
CLINIC = 'clinic'
UPLOADED_IMAGES = 'uploaded_images'
ADMIN = 'admin'
EMAIL = 'email'
IMAGES = 'images'
ID = 'id'
RATE_MEDIA = 'rate_media'

geolocator = Nominatim(user_agent="animal_health")

class ClinicImageSerializer(serializers.ModelSerializer):
    class Meta:
        model = ClinicImage
        fields = ['image', 'created_at', ID]

class ClinicSerializer(serializers.ModelSerializer):

    admin = serializers.HiddenField(default=serializers.CurrentUserDefault())
    rate_media = serializers.FloatField(read_only=True)

    images = ClinicImageSerializer(many=True, read_only=True)
    uploaded_images = serializers.ListField(
        child=serializers.ImageField(max_length=1000000, allow_empty_file=False, use_url=False),
        write_only=True,
        required=False
    )
    
    class Meta:
        model = Clinic
        fields = [ID, EMAIL, NAME, ADDRESS, UPLOADED_IMAGES, ADMIN, IMAGES, LATITUDE, LONGITUDE, RATE_MEDIA]
        read_only_fields = [LATITUDE, LONGITUDE, EMAIL, ID]

    def create(self, validated_data):
        user = validated_data.pop('admin')
        
        if not getattr(user, 'is_vet', False):
            raise serializers.ValidationError(
                {"detail": "Solo los usuarios con perfil veterinario pueden registrar clínicas."}
            )
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
        validated_data[ADMIN] = user
        validated_data[EMAIL] = user.email

        clinic = Clinic.objects.create(**validated_data)

        user.clinic_admin = True
        user.save()

        for image in images_data:
            ClinicImage.objects.create(clinic=clinic, image=image)

        return clinic