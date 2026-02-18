from rest_framework import permissions

class IsVetAndOnlyViewClients(permissions.BasePermission):
    def has_permission(self, request, view):
        return bool(request.user and request.user.is_authenticated and request.user.is_vet)

    def has_object_permission(self, request, view, obj):
        return not getattr(obj, 'is_vet', False)
    
class IsClinicAdminOfObject(permissions.BasePermission):
    def has_permission(self, request, view):
        return bool(
            request.user and 
            request.user.is_authenticated and 
            request.user.is_vet and 
            request.user.clinic_admin
        )

    def has_object_permission(self, request, view, obj):
        return obj.works_at and obj.works_at.admin == request.user