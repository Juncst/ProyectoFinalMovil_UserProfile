import type { Request, Response } from "express";
import { registerUser } from "../../services/auth/register.service.js";

export async function register(req: Request, res: Response) {
    const { full_name, email, password } = req.body ?? {};

    if (
        typeof full_name !== "string" ||
        typeof email !== "string" ||
        typeof password !== "string"
    ) {
        return res.status(400).json({ message: "Todos los campos son obligatorios" });
    }

    const fullName = full_name.trim();
    const normalizedEmail = email.trim().toLowerCase();

    if (
        [...fullName].length < 2 ||
        [...fullName].length > 100 ||
        fullName.includes("\0") ||
        normalizedEmail.length > 255 ||
        !/^[^\s@\x00-\x1f\x7f]+@[^\s@\x00-\x1f\x7f]+\.[^\s@\x00-\x1f\x7f]+$/.test(normalizedEmail) ||
        [...password].length < 8 ||
        Buffer.byteLength(password, "utf8") > 72
    ) {
        return res.status(400).json({
            message: "Verifica el nombre, correo y contraseña (mínimo 8 caracteres y máximo 72 bytes)",
        });
    }

    const user = await registerUser(fullName, normalizedEmail, password);
    if (!user) {
        return res.status(409).json({ message: "El correo electrónico ya está registrado" });
    }

    return res.status(201).json({ message: "Usuario registrado correctamente", user });
}
