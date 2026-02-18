from rest_framework import viewsets
from rest_framework.permissions import IsAuthenticatedOrReadOnly

from rates.models import Rate
from rates.serializers import RatesSerializer
from .permissions import IsOwnerOrReadOnly

class RateViewSet(viewsets.ModelViewSet):
    queryset = Rate.objects.all()
    serializer_class = RatesSerializer
    permission_classes = [IsAuthenticatedOrReadOnly, IsOwnerOrReadOnly]

    filterset_fields = ['clinic', 'user']