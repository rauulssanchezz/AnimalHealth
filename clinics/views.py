from requests import Response
from rest_framework import permissions, viewsets, filters
from .models import Clinic
from .serializers import ClinicSerializer
from .permissions import IsClinicAdmin, IsClinicAdminOfObject
from django.db.models import Avg, FloatField, ExpressionWrapper, F
from django.db.models.functions import Coalesce, Round, Cos, ACos, Radians, Sin
from rest_framework.decorators import action
from django_filters.rest_framework import DjangoFilterBackend

class ClinicViewSet(viewsets.ModelViewSet):
    serializer_class = ClinicSerializer
    queryset = Clinic.objects.all()

    filter_backends = [DjangoFilterBackend, filters.SearchFilter]
    filterset_fields = ['name', 'address']
    search_fields = ['name', 'address']

    def get_permissions(self):
        if self.action == 'create':
            return [IsClinicAdmin()]
        
        elif self.action in ['update', 'partial_update', 'destroy']:
            return [IsClinicAdminOfObject()]
        
        return [permissions.AllowAny()]

    def perform_create(self, serializer):
        serializer.save()

    def get_queryset(self):
        return Clinic.objects.annotate(
            rate_media=Round(Coalesce(Avg('rates__rate'), 0.0), 1)
        ).order_by('-rate_media')

    @action(detail=False, methods=['get'])
    def nearby(self, request):
        lat = request.query_params.get('latitude', None)
        lon = request.query_params.get('longitude', None)

        if lat == None or lon == None:
            return queryset

        dist_max = request.query_params.get('distance', 10)

        if not lat or not lon:
            return Response(
                {"error": "Se requieren los parámetros 'latitude' y 'longitude'"}, 
                status=400
            )

        try:
            u_lat, u_lon, u_dist = float(lat), float(lon), float(dist_max)
            
            queryset = Clinic.objects.annotate(
                rate_media=Round(Coalesce(Avg('rates__rate'), 0.0), 1),
                distancia=ExpressionWrapper(
                    6371 * ACos(
                        Cos(Radians(u_lat)) * Cos(Radians(F('latitude'))) *
                        Cos(Radians(F('longitude')) - Radians(u_lon)) +
                        Sin(Radians(u_lat)) * Sin(Radians(F('latitude')))
                    ),
                    output_field=FloatField()
                )
            ).filter(distancia__lte=u_dist).order_by('distancia')

            serializer = self.get_serializer(queryset, many=True)
            return Response(serializer.data)

        except (ValueError, TypeError):
            return Response({"error": "Parámetros numéricos inválidos"}, status=400)