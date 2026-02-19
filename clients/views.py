from rest_framework import viewsets
from clients.models import Client
from clients.serializers import ClientSerializer
from rest_framework.permissions import IsAuthenticated
from clients.permissions import IsClientOrWorker
from django_filters.rest_framework import DjangoFilterBackend

class ClientViewSet(viewsets.ModelViewSet):
    serializer_class = ClientSerializer
    permission_classes = IsAuthenticated, IsClientOrWorker

    def get_queryset(self):
        user = self.request.user
        if user.is_vet:
            return Client.objects.filter(clinic=user.works_at)
        
        return Client.objects.filter(user=user)
    
    filter_backends = [DjangoFilterBackend]
    filterset_fields = ['user', 'user__username', 'clinic', 'clinic__name']
