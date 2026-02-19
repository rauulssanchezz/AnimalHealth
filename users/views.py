from rest_framework import generics, status, permissions, viewsets
from rest_framework.response import Response
from rest_framework.views import APIView
from django_filters.rest_framework import DjangoFilterBackend
from users.models import User
from users.permissions import IsClinicAdminOfObject, IsVetAndOnlyViewClients
from .serializers import RegisterSerializer, UserPublicSerializer, VetPublicSerializer

class RegisterView(APIView):
    permission_classes=[permissions.AllowAny]

    def post(self, request):
        serializer = RegisterSerializer(data=request.data)

        if serializer.is_valid():
            serializer.save()
            return Response({"message": "Usuario creado con éxito"}, status=status.HTTP_201_CREATED)
        
        return Response(serializer.errors, status=status.HTTP_400_BAD_REQUEST)
    
class UserProfileView(generics.RetrieveUpdateDestroyAPIView):
    serializer_class = RegisterSerializer
    permission_classes = [permissions.IsAuthenticated]

    def get_object(self):
        return self.request.user
    
class UserInformationView(generics.RetrieveAPIView):
    queryset = User.objects.all()
    serializer_class = UserPublicSerializer
    permission_classes = [permissions.IsAuthenticated, IsVetAndOnlyViewClients]

    filter_backends = [DjangoFilterBackend]
    filterset_fields = ['username', 'email']

class VetViewSet(viewsets.ModelViewSet):
    serializer_class = VetPublicSerializer
    permission_classes = [permissions.IsAuthenticated, IsClinicAdminOfObject]

    def get_queryset(self):
        return User.objects.filter(works_at__admin=self.request.user)
    
    filter_backends = [DjangoFilterBackend]
    filterset_fields = ['username', 'email', 'clinic']
    
class LogoutView(APIView):
    permission_classes = [permissions.IsAuthenticated]

    def post(self, request):
        request.user.auth_token.delete()
        return Response({"detail": "Sesión cerrada correctamente."}, status=status.HTTP_200_OK)