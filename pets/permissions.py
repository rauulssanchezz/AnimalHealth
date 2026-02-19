from rest_framework import permissions

class IsOwnerOrVet(permissions.BasePermission):
    def has_object_permission(self, request, views, obj):
        return bool(obj.owner == request.user or request.user.is_vet)
    
class IsVet(permissions.BasePermission):
    def has_object_permission(self, request, views, obj):
        return bool(request.user.is_vet)