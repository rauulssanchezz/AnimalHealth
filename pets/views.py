from rest_framework import viewsets
from pets.models import Pet
from pets.serializers import PetBaseSerializer, PetVetSerializer
from rest_framework.permissions import IsAuthenticated
from pets.permissions import IsOwnerOrVet, IsVet
from django_filters.rest_framework import DjangoFilterBackend

class PetViewSet(viewsets.ModelViewSet):
    queryset = Pet.objects.all()
    serializer_class = PetBaseSerializer
    permission_classes = IsAuthenticated, IsOwnerOrVet

    def get_queryset(self):
        user = self.request.user
        if getattr(user, 'is_vet', False):
            return Pet.objects.all()

        return Pet.objects.filter(owner=user)

    def get_serializer_class(self):
        if getattr(self.request.user, 'is_vet', False):
            return PetVetSerializer
        return PetBaseSerializer

    filter_backends = [DjangoFilterBackend]
    filterset_fields = ['name', 'owner__username', 'chip', 'owner']
