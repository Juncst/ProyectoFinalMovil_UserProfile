import type { RequestHandler } from "express";
import jwt from "jsonwebtoken";
import { config } from "../config.js";

export const requireAuth: RequestHandler = (req, res, next) => {
    const token = /^Bearer ([^\s]+)$/i.exec(req.get("Authorization") ?? "")?.[1];
    if (!token) {
        res.status(401).json({ message: "Se requiere un token Bearer" });
        return;
    }

    try {
        const payload = jwt.verify(token, config.jwtSecret, { algorithms: ["HS256"] });
        if (
            typeof payload !== "object" ||
            typeof payload.exp !== "number" ||
            !/^[1-9]\d{0,9}$/.test(payload.sub ?? "") ||
            Number(payload.sub) > 2147483647
        ) {
            res.status(401).json({ message: "Token inválido o expirado" });
            return;
        }

        res.locals.userId = Number(payload.sub);
    } catch {
        res.status(401).json({ message: "Token inválido o expirado" });
        return;
    }

    next();
};
