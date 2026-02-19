from rest_framework import permissions
from pets.permissions import IsVet

class IsClientOrWorker(permissions.BasePermission):
    def has_object_permission(self, request, view, obj):
        user = request.user
        return bool(user.is_vet and user.works_at == obj.clinic or user == obj.user)