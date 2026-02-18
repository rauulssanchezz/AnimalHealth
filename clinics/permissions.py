from rest_framework import permissions
    
class IsClinicAdmin(permissions.BasePermission):
    def has_permission(self, request, view):
        return bool(
            request.user and 
            request.user.is_authenticated and 
            getattr(request.user, 'is_vet', False) and
            request.user and 
            request.user.is_authenticated and 
            request.user.clinic_admin
        )

class IsClinicAdminOfObject(permissions.BasePermission):

    def has_permission(self, request, view):
        return bool(
            request.user and 
            request.user.is_authenticated and 
            request.user.clinic_admin 
        )

    def has_object_permission(self, request, view, obj):
        return bool(
            request.user == obj.admin
        )