from rest_framework import permissions, viewsets
from .models import Clinic
from .serializers import ClinicSerializer
from .permissions import IsVeterinary, IsClinicAdmin

class ClinicView(viewsets.ModelViewSet):
    queryset = Clinic.objects.all()
    serializer_class = ClinicSerializer

    def get_permissions(self):
        if self.action == 'create':
            return [IsVeterinary()]
        
        elif self.action in ['update', 'partial_update', 'destroy']:
            return [IsClinicAdmin()]
        
        return [permissions.AllowAny()]

    def perform_create(self, serializer):
        serializer.save()